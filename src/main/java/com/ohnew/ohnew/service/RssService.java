package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.req.NewsArticleReq;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RssService {

    private static final List<String> RSS_URLS = List.of(
            "https://rss.cnn.com/rss/edition.rss",
            "https://feeds.bbci.co.uk/news/rss.xml",
            "https://rss.donga.com/total.xml",
            "http://www.yonhapnews.co.kr/RSS/economy.xml"
    );

    /**
     * RSS 피드에서 뉴스 기사들을 파싱하여 NewsArticleReq 리스트로 반환
     */
    public List<NewsArticleReq> fetchNewsFromRss() {
        List<NewsArticleReq> articles = new ArrayList<>();
        
        for (String rssUrl : RSS_URLS) {
            try {
                log.info("RSS 피드 파싱 시작: {}", rssUrl);
                articles.addAll(parseRssFeed(rssUrl));
            } catch (Exception e) {
                log.error("RSS 피드 파싱 실패: {}, 오류: {}", rssUrl, e.getMessage());
            }
        }
        
        log.info("총 {}개의 뉴스 기사 파싱 완료", articles.size());
        return articles;
    }

    /**
     * 특정 RSS URL에서 뉴스 기사들을 파싱
     */
    private List<NewsArticleReq> parseRssFeed(String rssUrl) throws Exception {
        List<NewsArticleReq> articles = new ArrayList<>();
        
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));
        
        for (SyndEntry entry : feed.getEntries()) {
            try {
                String title = entry.getTitle();
                String link = entry.getLink();
                String description = entry.getDescription() != null 
                    ? entry.getDescription().getValue() 
                    : "";
                
                // HTML 태그 제거
                String cleanBody = cleanHtmlTags(description);
                
                // 본문이 너무 짧으면 링크에서 추가 내용 추출 시도
                if (cleanBody.length() < 100 && link != null) {
                    cleanBody = extractContentFromUrl(link, cleanBody);
                }
                
                NewsArticleReq article = NewsArticleReq.builder()
                        .articleId(generateArticleId(link))
                        .title(title)
                        .body(cleanBody)
                        .build();
                
                articles.add(article);
                
            } catch (Exception e) {
                log.warn("뉴스 기사 파싱 중 오류 발생: {}", e.getMessage());
            }
        }
        
        return articles;
    }

    /**
     * HTML 태그 제거 및 텍스트 정리
     */
    private String cleanHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        // Jsoup을 사용하여 HTML 태그 제거
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        
        // 연속된 공백 제거
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * URL에서 추가 컨텐츠 추출 (간단한 버전)
     */
    private String extractContentFromUrl(String url, String fallbackContent) {
        try {
            // 타임아웃 설정으로 빠른 추출
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            
            // 주요 컨텐츠 영역에서 텍스트 추출
            String content = doc.select("article, .content, .article-body, p").text();
            
            if (content.length() > 100) {
                // 너무 긴 경우 적절히 자르기 (2000자 제한)
                return content.length() > 2000 ? content.substring(0, 2000) + "..." : content;
            }
            
        } catch (Exception e) {
            log.debug("URL에서 컨텐츠 추출 실패: {}", url);
        }
        
        return fallbackContent;
    }

    /**
     * 링크를 기반으로 고유한 기사 ID 생성
     */
    private String generateArticleId(String link) {
        if (link == null) {
            return String.valueOf(System.currentTimeMillis());
        }
        
        // URL의 해시코드를 사용하여 간단한 ID 생성
        return String.valueOf(Math.abs(link.hashCode()));
    }
}
