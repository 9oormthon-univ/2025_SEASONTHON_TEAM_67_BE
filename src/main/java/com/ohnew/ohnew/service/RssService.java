package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.req.RssAIReq;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssService {

    private static final Logger logger = LoggerFactory.getLogger(RssService.class);

    private final WebClient webClient;

    public Flux<RssAIReq> fetchRssFeedAndCrawl(String rssUrl) {
        return webClient.get()
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
                .flatMap(entry -> {
                    String articleUrl = entry.getUri() != null ? entry.getUri() : entry.getLink();

                    return webClient.get()
                            .uri(articleUrl)
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(html -> {
                                // Jsoup으로 HTML 파싱
                                Document doc = Jsoup.parse(html);
                                // 본문 추출 ( text_area 클래스 연결)
                                Elements textAreas = doc.select(".text_area");
                                // <br> 태그 줄 삭제
                                textAreas.select("br").remove();

                                String body = textAreas.text().trim();

                                return new RssAIReq(articleUrl, entry.getTitle(), body);
                            })
                            .onErrorResume(ex -> {
                                logger.warn("기사 크롤링 실패: {}, error: {}", articleUrl, ex.getMessage());
                                return Mono.just(new RssAIReq(articleUrl, entry.getTitle(), ""));
                            });
                });
    }
}