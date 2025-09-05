package com.ohnew.ohnew.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohnew.ohnew.dto.res.RssNewsMultiRes;
import com.ohnew.ohnew.service.RssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RssTestRunner implements CommandLineRunner {

    @Autowired
    private RssService rssService;

    @Override
    public void run(String... args) throws Exception {

        RssNewsMultiRes response = rssService.fetchAndDisplayRssData();
        
        // JSON 형태로 출력
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        
        System.out.println("\n=== RSS 데이터 JSON 형태 ===");
        System.out.println(jsonResponse);
    }
}
