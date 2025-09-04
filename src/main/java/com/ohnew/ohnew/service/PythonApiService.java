package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.req.ChatReq;
import com.ohnew.ohnew.dto.req.NewsArticleReq;
import com.ohnew.ohnew.dto.req.NewsArticleBatchReq;
import com.ohnew.ohnew.dto.res.ChatRes;
import com.ohnew.ohnew.dto.res.NewsArticleRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PythonApiService {

    @Value("${python.api.url:http://localhost:8000}")
    private String pythonApiUrl;

    @Value("${python.api.single.endpoint:/v1/rewrite-summarize}")
    private String singleArticleEndpoint;

    @Value("${python.api.batch.endpoint:/v1/rewrite-batch}")
    private String batchArticleEndpoint;

    @Value("${python.api.chat.endpoint:/v1/chat-article}")
    private String chatArticleEndpoint;

    private final WebClient webClient;

    /**
     * 파이썬 API에 뉴스 기사 리스트를 전송하여 AI 처리 결과를 받아옴 (배치 처리)
     */
    public List<NewsArticleRes.NewsArticleDetail> processNewsWithAI(List<NewsArticleReq> newsArticles) {
        try {
            log.info("파이썬 API에 {}개의 뉴스 기사 전송 시작", newsArticles.size());

            NewsArticleBatchReq request = NewsArticleBatchReq.builder()
                    .items(newsArticles)
                    .build();

            String fullUrl = pythonApiUrl + batchArticleEndpoint;
            log.info("파이썬 API 호출 URL: {}", fullUrl);

            NewsArticleRes response = webClient.post()
                    .uri(fullUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(request), NewsArticleBatchReq.class)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> {
                            log.error("파이썬 API 4xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 클라이언트 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> {
                            log.error("파이썬 API 5xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 서버 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .bodyToMono(NewsArticleRes.class)
                    .timeout(Duration.ofMinutes(5)) // 5분 타임아웃
                    .block();

            if (response != null && response.getProcessedArticles() != null) {
                log.info("파이썬 API에서 {}개의 처리된 기사 수신", response.getProcessedArticles().size());
                return response.getProcessedArticles();
            } else {
                log.warn("파이썬 API에서 빈 응답 수신");
                return List.of();
            }

        } catch (Exception e) {
            log.error("파이썬 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파이썬 API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 단일 뉴스 기사 처리
     */
    public NewsArticleRes.NewsArticleDetail processSingleNewsWithAI(NewsArticleReq newsArticle) {
        try {
            log.info("파이썬 API에 단일 뉴스 기사 전송: {}", newsArticle.getTitle());

            String fullUrl = pythonApiUrl + singleArticleEndpoint;
            log.info("파이썬 API 호출 URL: {}", fullUrl);

            NewsArticleRes.NewsArticleDetail response = webClient.post()
                    .uri(fullUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(newsArticle), NewsArticleReq.class)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> {
                            log.error("파이썬 API 4xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 클라이언트 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> {
                            log.error("파이썬 API 5xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 서버 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .bodyToMono(NewsArticleRes.NewsArticleDetail.class)
                    .timeout(Duration.ofMinutes(2)) // 2분 타임아웃
                    .block();

            if (response != null) {
                log.info("파이썬 API에서 단일 기사 처리 완료: {}", response.getTitle());
                return response;
            } else {
                log.warn("파이썬 API에서 빈 응답 수신");
                throw new RuntimeException("파이썬 API에서 빈 응답을 받았습니다.");
            }

        } catch (Exception e) {
            log.error("파이썬 API 단일 기사 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파이썬 API 단일 기사 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 채팅 기능 - 기사에 대한 질문과 답변
     */
    public ChatRes chatWithArticle(ChatReq chatRequest) {
        try {
            log.info("파이썬 API에 채팅 요청 전송: 기사 ID {}, 사용자 ID {}, 메시지: {}", 
                chatRequest.getArticleId(), chatRequest.getUserId(), chatRequest.getUserMessage());

            String fullUrl = pythonApiUrl + chatArticleEndpoint;
            log.info("파이썬 API 채팅 호출 URL: {}", fullUrl);

            ChatRes response = webClient.post()
                    .uri(fullUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(chatRequest), ChatReq.class)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError(),
                        clientResponse -> {
                            log.error("파이썬 API 채팅 4xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 채팅 클라이언트 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .onStatus(
                        status -> status.is5xxServerError(),
                        clientResponse -> {
                            log.error("파이썬 API 채팅 5xx 오류: {}", clientResponse.statusCode());
                            return Mono.error(new RuntimeException("파이썬 API 채팅 서버 오류: " + clientResponse.statusCode()));
                        }
                    )
                    .bodyToMono(ChatRes.class)
                    .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                    .block();

            if (response != null) {
                log.info("파이썬 API 채팅 응답 수신: {}", response.getAnswer());
                return response;
            } else {
                log.warn("파이썬 API 채팅에서 빈 응답 수신");
                return ChatRes.builder()
                        .success(false)
                        .message("AI로부터 응답을 받지 못했습니다.")
                        .articleId(chatRequest.getArticleId())
                        .build();
            }

        } catch (Exception e) {
            log.error("파이썬 API 채팅 중 오류 발생: {}", e.getMessage(), e);
            return ChatRes.builder()
                    .success(false)
                    .message("채팅 처리 중 오류가 발생했습니다: " + e.getMessage())
                    .articleId(chatRequest.getArticleId())
                    .build();
        }
    }

    /**
     * 파이썬 API 서버 상태 확인
     */
    public boolean checkPythonApiHealth() {
        try {
            String healthUrl = pythonApiUrl + "/health";
            
            String response = webClient.get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("파이썬 API 상태 확인 성공: {}", response);
            return true;

        } catch (Exception e) {
            log.error("파이썬 API 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}
