package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RssServiceImpl {

    private static final String RSS_URL = "https://news.sbs.co.kr/news/TopicRssFeed.do?plink=RSSREADER";
    private final NewsRepository newsRepository;
    private final WebClient webClient = WebClient.builder().build();
    private final  NewsVariantService newsVariantService;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6시간 *
    @Transactional
    public void fetchAndProcessRss() {
        try {
            // RSS 읽고 신규 News 저장
            List<NewsByRssRes> items = fetchAndSaveRssData();

            if (items.isEmpty()) {
                log.info("신규 RSS 항목 없음");
                return;
            }

            // 파이썬 호출 + 변형/퀴즈/질문 저장
            NewsByMultiRssRes multiRes = NewsByMultiRssRes.builder()
                    .items(items)
                    .build();

            callPythonApi(multiRes);

        } catch (Exception e) {
            log.error("RSS 처리 중 오류", e);
        }
    }

    /**
     * RSS 데이터를 가져와서 DB에 저장하고, DTO 리스트 반환
     */
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

                System.out.println("Title: " + title);
                System.out.println("Link: " + link);
                System.out.println("Tags: " + category);

                // 이미 DB에 있으면 skip
                if (newsRepository.existsByOriginalUrl(link)) {
                    log.info("이미 저장된 기사 {}", link);
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
            log.error("RSS 데이터 파싱 중 오류 발생: ", e);
        }
        return newsByRssResList;
    }

    /**
     * 파이썬 API 요청
      */
    private void callPythonApi(NewsByMultiRssRes multiRes) {
        try {
            NewsByPythonRes pythonRes = webClient.post()
                    .uri("http://localhost:8000/v1/rewrite-batch3") // ★ 교체
                    .bodyValue(multiRes)
                    .retrieve()
                    .bodyToMono(NewsByPythonRes.class)
                    .block();

            if (pythonRes != null && pythonRes.getResults() != null) {
                pythonRes.getResults().forEach(item -> {
                    if (item.getOk() == null || !item.getOk()) {
                        log.warn("Python 처리 실패 articleId={} error={}", item.getArticleId(), item.getError());
                        return;
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
                                log.warn("알 수 없는 newsStyle: {}", v.getNewsStyle());
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
        } catch (Exception e) {
            log.error("Python API 호출 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.AI_PROCESSING_FAILED);
        }
    }


    /**
     * 기사 웹 크롤링
     */
    private String extractTextAreaContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            Element textAreaElement = doc.selectFirst(".text_area");
            return (textAreaElement != null) ? textAreaElement.text() : "본문 내용을 찾을 수 없습니다.";

        } catch (Exception e) {
            log.error("웹페이지 파싱 중 오류 발생: " + url, e);
            return "본문 내용을 가져오는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
