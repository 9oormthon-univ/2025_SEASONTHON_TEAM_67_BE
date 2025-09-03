package com.ohnew.ohnew.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RssRunService implements CommandLineRunner {

    private final RssService rssService;

    @Override
    public void run(String... args) throws Exception {
        String url = "https://news.sbs.co.kr/news/TopicRssFeed.do?plink=RSSREADER";

        rssService.fetchRssFeedAndCrawl(url)
                .subscribe(article -> {
                    System.out.println("ArticleId: " + article.getArticleId());
                    System.out.println("Title: " + article.getTitle());
                    System.out.println("body: " + article.getBody());
                    System.out.println("----------------------------");
                });

        System.out.println("RSS 요청 후 바로 이 메시지가 출력됩니다 (비동기 처리 확인).");
    }
}