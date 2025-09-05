package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.res.NewsByPythonRes;
import com.ohnew.ohnew.dto.res.NewsByRssMultiRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class NewsAiService {

    private final WebClient webClient = WebClient.builder().build();

    public NewsByPythonRes sendArticlesToPython(NewsByRssMultiRes rssNewsMultiRes) {
        return webClient.post()
                .uri("http://localhost:8000/analyze")
                .bodyValue(rssNewsMultiRes)
                .retrieve()
                .bodyToMono(NewsByPythonRes.class)
                .block(); // 동기식으로 결과 대기 (비동기로 원하면 .subscribe() 사용)
    }
}
