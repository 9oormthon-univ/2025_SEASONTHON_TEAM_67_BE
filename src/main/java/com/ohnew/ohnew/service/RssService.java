package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.res.RssNewsRes;
import com.ohnew.ohnew.dto.res.RssNewsMultiRes;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RssService {

    private static final String RSS_URL = "https://news.sbs.co.kr/news/TopicRssFeed.do?plink=RSSREADER";

    public RssNewsMultiRes fetchAndDisplayRssData() {
        try {
            // RSS 피드 읽기
            URL feedUrl = new URL(RSS_URL);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            List<SyndEntry> entries = feed.getEntries();
            List<RssNewsRes> articles = new ArrayList<>();
            
            System.out.println("=== SBS RSS 뉴스 데이터 ===");
            System.out.println("총 " + entries.size() + "개의 뉴스를 찾았습니다.\n");

            for (int i = 0; i < entries.size(); i++) {
                SyndEntry entry = entries.get(i);
                
                String title = entry.getTitle();
                String guid = entry.getUri();
                String link = entry.getLink();
                
                // 카테고리 추출
                String category = "";
                if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
                    category = entry.getCategories().get(0).getName();
                }

                // 링크에서 .text_area 영역의 데이터 추출
                String textAreaContent = extractTextAreaContent(link);

                // DTO 생성
                RssNewsRes article = RssNewsRes.builder()
                        .articleId(String.valueOf(i + 1))
                        .title(title)
                        .body(textAreaContent)
                        .build();
                
                articles.add(article);
            }

            return RssNewsMultiRes.builder()
                    .items(articles)
                    .build();

        } catch (Exception e) {
            log.error("RSS 데이터를 가져오는 중 오류 발생: ", e);
            System.out.println("RSS 데이터를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            
            // 오류 발생 시 빈 응답 반환
            return RssNewsMultiRes.builder()
                    .items(new ArrayList<>())
                    .build();
        }
    }

    private String extractTextAreaContent(String url) {
        try {
            // Jsoup을 사용하여 웹페이지 파싱
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // .text_area 클래스를 가진 요소 찾기
            Element textAreaElement = doc.selectFirst(".text_area");
            
            if (textAreaElement != null) {
                // HTML 태그 제거하고 텍스트만 추출
                return textAreaElement.text();
            } else {
                return "본문 내용을 찾을 수 없습니다.";
            }

        } catch (Exception e) {
            log.error("웹페이지 파싱 중 오류 발생: " + url, e);
            return "본문 내용을 가져오는 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
