package com.ohnew.ohnew.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RssTestRunner implements CommandLineRunner {

    @Autowired
    private RssService rssService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("RSS 테스트를 시작합니다...");
        rssService.fetchAndDisplayRssData();
    }
}
