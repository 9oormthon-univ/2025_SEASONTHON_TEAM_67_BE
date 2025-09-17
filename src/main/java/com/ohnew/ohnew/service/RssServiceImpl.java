package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.common.PythonApi;
import com.ohnew.ohnew.dto.res.NewsByPythonRes;
import com.ohnew.ohnew.dto.res.NewsByRssRes;
import com.ohnew.ohnew.dto.res.NewsByMultiRssRes;
import com.ohnew.ohnew.entity.News;
import com.ohnew.ohnew.repository.NewsRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URL;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RssServiceImpl {

    // RSS 주소는 보안과 상관이 없으므로 하드코딩.
    private static final String RSS_URL = "https://news.sbs.co.kr/news/TopicRssFeed.do?plink=RSSREADER";
    private final NewsRepository newsRepository;
    private final PythonApi  pythonApi;
    private final WebClient webClient = WebClient.builder().build();
    private final  NewsVariantService newsVariantService;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6시간 *
    @Transactional
    public void fetchAndProcessRss() {
        try {
            // RSS 읽고 신규 News 저장
            List<NewsByRssRes> items = fetchAndSaveRssData();

            if (items.isEmpty()) {
                log.info("업데이트된 RSS 항목 없음");
                return;
            }

            // 파이썬 호출 + 변형/퀴즈/질문 저장
            NewsByMultiRssRes multiRes = NewsByMultiRssRes.builder()
                    .items(items)
                    .build();

            callPythonApi(multiRes);

        } catch (Exception e) {
            log.error("RSS 새로고침 실패: {}", e.getMessage());
            throw new GeneralException(ErrorStatus.RSS_FETCH_FAILED);
        }
    }

    //RSS 데이터를 가져와서 DB에 저장하고, DTO 리스트 반환
    private List<NewsByRssRes> fetchAndSaveRssData() {
        List<NewsByRssRes> newsByRssResList = new ArrayList<>();
        try {
            URL feedUrl = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getUri();
                String body = extractTextAreaContent(link);
                String category = (entry.getCategories() != null && !entry.getCategories().isEmpty())
                        ? entry.getCategories().get(0).getName()
                        : "";
                Date pubDate = entry.getPublishedDate();

                log.info("----- DB에 저장된 데이터 ----");
                log.info("제목: {}", title);
                log.info("Link: {}", link);
                log.info("Tags: {}", category);
                log.info("--------------------------");

                // 이미 DB에 있으면 skip
                if (newsRepository.existsByOriginalUrl(link)) {
                    log.info("이미 저장된 기사 링크: {}", link);
                    continue;
                }

                // DB 저장
                News savedNews = newsRepository.save(
                        News.builder()
                                .originalUrl(link)
                                .originalPublishedAt(pubDate.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate())
                                .tags(Collections.singleton(category))
                                .build()
                );

                // DTO에 담기
                newsByRssResList.add(NewsByRssRes.builder()
                        .articleId(savedNews.getId().toString())
                        .title(title)
                        .body(body)
                        .build());

            }
        } catch (Exception e) {
            log.error("DB 저장 실패: {}", e.getMessage());
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
        return newsByRssResList;
    }

    // 파이썬 요청
    private void callPythonApi(NewsByMultiRssRes multiRes) {
        // Python API 호출 (10분 타임아웃 포함)
        NewsByPythonRes pythonRes = pythonApi.callPythonApi(
                "http://localhost:8000/v1/rewrite-batch3",
                multiRes,
                NewsByPythonRes.class
        );

        if (pythonRes != null && pythonRes.getResults() != null) {
            pythonRes.getResults().forEach(item -> {
                if (item.getOk() == null || !item.getOk()) {
                    log.error("Python 처리 실패 articleId={} error={}", item.getArticleId(), item.getError());
                    throw new GeneralException(ErrorStatus.AI_PROCESSING_FAILED);
                }
                var data = item.getData();
                if (data == null) return;

                Long newsId = Long.parseLong(item.getArticleId());

                if (data.getVariants() != null) {
                    data.getVariants().forEach(v -> {
                        try {
                            var style = com.ohnew.ohnew.entity.enums.NewsStyle.valueOf(v.getNewsStyle().toUpperCase());
                            newsVariantService.upsertVariant(
                                    newsId,
                                    style,
                                    v.getNewTitle(),
                                    v.getSummary(),
                                    v.getEpi()!=null ? v.getEpi().getStimulationReduced() : null,
                                    v.getEpi()!=null ? v.getEpi().getReason() : null
                            );
                        } catch (IllegalArgumentException e) {
                            log.error("알 수 없는 newsStyle: {}", e.getMessage());
                            throw new GeneralException(ErrorStatus.AI_PROCESSING_FAILED);
                        }
                    });
                }

                // 부모 News에 질문/퀴즈 저장
                newsRepository.findById(newsId).ifPresent(n -> {
                    n.replaceQuestions(data.getQuestions()); // 정규화 + 최대 4개 보장
                    if (data.getQuiz() != null) {
                        n.setQuiz(data.getQuiz().getQuestion(), data.getQuiz().getAnswer());
                    } else {
                        n.clearQuiz(); // 퀴즈가 없으면 비우기
                    }
                });
            });
        }
    }

    // 기사 내용 크롤링
    private String extractTextAreaContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Element textAreaElement = doc.selectFirst(".text_area");
            if (textAreaElement == null) {
                log.warn("뉴스 기사 데이터 크롤링 실패");
                throw new GeneralException(ErrorStatus.NEWS_ARTICLE_NOT_FOUND);
            }
            return textAreaElement.text();
        } catch (Exception e) {
            log.error("웹페이지 파싱 중 오류 발생 → url: {}", url);
            throw new GeneralException(ErrorStatus.NEWS_SCRAPING_FAILED);
        }
    }
}
