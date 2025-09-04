package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.req.RssAIReq;
import com.ohnew.ohnew.entity.NewsArticle;
import com.ohnew.ohnew.repository.NewsArticleRepository;
import com.ohnew.ohnew.dto.req.NewsArticleReq;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssService {

    private static final Logger logger = LoggerFactory.getLogger(RssService.class);

    private final WebClient webClient;
    private final NewsArticleRepository newsArticleRepository;

    public void fetchRssLinksAndSave(String rssUrl, String pythonApiUrl) {
        webClient.get()
                .uri(rssUrl)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(xml -> {
                    try {
                        SyndFeedInput input = new SyndFeedInput();
                        List<SyndEntry> entries = input.build(
                                new XmlReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                        ).getEntries();

                        return Flux.fromIterable(entries);
                    } catch (Exception e) {
                        logger.error("RSS 파싱 중 예외 발생: {}", e.getMessage(), e);
                        return Flux.empty();
                    }
                })
                .collectList() // 🔑 여기서 리스트로 모아서 한번에 처리 가능
                .subscribe(entries -> {
                    List<NewsArticleReq> requestList = new ArrayList<>();

                    for (SyndEntry entry : entries) {
                        String link = entry.getUri() != null ? entry.getUri() : entry.getLink();

                        if (!newsArticleRepository.existsByLink(link)) {
                            NewsArticle article = new NewsArticle();
                            article.setLink(link);
                            newsArticleRepository.save(article);

                            logger.info("링크 저장: {}", link);

                            // 개별 API 호출용 DTO
                            NewsArticleReq req = NewsArticleReq.builder()
                                    .articleId(String.valueOf(article.getId()))
                                    .title(entry.getTitle())
                                    .build();

                            requestList.add(req);

                            // 개별 호출 (원래 방식)
                            sendSingleArticleToAI(req, pythonApiUrl, article);
                        }
                    }

                    // 여러 개 한꺼번에 전송
                    if (!requestList.isEmpty()) {
                        sendBatchArticlesToAI(requestList, pythonApiUrl);
                    }
                });
    }

    private void sendSingleArticleToAI(NewsArticleReq req, String pythonApiUrl, NewsArticle article) {
        webClient.post()
                .uri(pythonApiUrl)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(NewsArticleReq.class)
                .subscribe(result -> {
                    article.setTitle(result.getTitle());
                    article.setSummary(result.getBody());
                    article.setAiProcessed(true);
                    newsArticleRepository.save(article);
                    logger.info("AI 처리 후 업데이트 완료: {}", article.getId());
                }, ex -> {
                    logger.warn("AI 처리 실패: {}, error: {}", article.getId(), ex.getMessage());
                });
    }

    private void sendBatchArticlesToAI(List<NewsArticleReq> requestList, String pythonApiUrl) {
        RssAIReq batchReq = RssAIReq.builder()
                .items(requestList)
                .build();

        webClient.post()
                .uri(pythonApiUrl + "/batch") // ✅ 배치 처리 엔드포인트 예시
                .bodyValue(batchReq)
                .retrieve()
                .bodyToMono(Void.class) // 파이썬 쪽에서 응답 형식에 맞게 수정
                .doOnSuccess(result -> logger.info("배치 AI 처리 완료. 기사 개수: {}", requestList.size()))
                .doOnError(  ex -> logger.error("배치 AI 처리 실패: {}", ex.getMessage()))
                .subscribe();
    }
}