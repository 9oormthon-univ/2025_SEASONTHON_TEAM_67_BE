package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.dto.req.ChatReq;
import com.ohnew.ohnew.dto.req.ChatMessageReq;
import com.ohnew.ohnew.dto.req.NewsArticleReq;
import com.ohnew.ohnew.dto.req.NewsArticleBatchReq;
import com.ohnew.ohnew.dto.res.ChatRes;
import com.ohnew.ohnew.dto.res.NewsArticleRes;
import com.ohnew.ohnew.service.NewsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Slf4j
@Tag(name = "뉴스 기사 API", description = "RSS 뉴스 수집 및 AI 분석 관련 API")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    @Operation(summary = "RSS 뉴스 수집 및 AI 처리", description = "RSS 피드에서 뉴스를 수집하고 AI로 분석하여 저장합니다.")
    @PostMapping("/process-rss")
    public ApiResponse<String> processRssNews() {
        try {
            // 비동기로 RSS 뉴스 처리 시작
            newsArticleService.processRssNewsWithAI();
            log.info("RSS 뉴스 처리 작업이 시작되었습니다.");
            
            return ApiResponse.onSuccess("RSS 뉴스 처리가 시작되었습니다. 백그라운드에서 진행됩니다.");
        } catch (Exception e) {
            log.error("RSS 뉴스 처리 시작 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("PROCESS_RSS_FAILED", "RSS 뉴스 처리 시작에 실패했습니다: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "뉴스 기사 상세 조회", description = "특정 뉴스 기사의 상세 정보를 조회합니다.")
    @GetMapping("/{articleId}")
    public ApiResponse<NewsArticleRes.NewsArticleDetail> getNewsArticleDetail(
            @Parameter(description = "기사 ID") @PathVariable Long articleId) {
        try {
            NewsArticleRes.NewsArticleDetail article = newsArticleService.getNewsArticleDetail(articleId);
            return ApiResponse.onSuccess(article);
        } catch (Exception e) {
            log.error("뉴스 기사 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("ARTICLE_NOT_FOUND", "뉴스 기사를 찾을 수 없습니다.", null);
        }
    }

    @Operation(summary = "뉴스 기사 목록 조회", description = "페이징된 뉴스 기사 목록을 조회합니다.")
    @GetMapping("/list")
    public ApiResponse<NewsArticleRes.NewsArticleListRes> getNewsArticleList(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            NewsArticleRes.NewsArticleListRes result = newsArticleService.getNewsArticleList(pageable);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("뉴스 기사 목록 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("LIST_FETCH_FAILED", "뉴스 기사 목록 조회에 실패했습니다.", null);
        }
    }

    @Operation(summary = "태그별 뉴스 기사 조회", description = "특정 태그의 뉴스 기사 목록을 조회합니다.")
    @GetMapping("/tag/{tag}")
    public ApiResponse<NewsArticleRes.NewsArticleListRes> getNewsArticlesByTag(
            @Parameter(description = "태그명") @PathVariable String tag,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            NewsArticleRes.NewsArticleListRes result = newsArticleService.getNewsArticlesByTag(tag, pageable);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("태그별 뉴스 기사 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("TAG_SEARCH_FAILED", "태그별 뉴스 검색에 실패했습니다.", null);
        }
    }

    @Operation(summary = "뉴스 기사 검색", description = "키워드로 뉴스 기사를 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<NewsArticleRes.NewsArticleListRes> searchNewsArticles(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            NewsArticleRes.NewsArticleListRes result = newsArticleService.searchNewsArticles(keyword, pageable);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("뉴스 기사 검색 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("SEARCH_FAILED", "뉴스 기사 검색에 실패했습니다.", null);
        }
    }

    @Operation(summary = "최근 뉴스 요약", description = "최근 7일간의 뉴스 기사 요약을 조회합니다.")
    @GetMapping("/recent-summary")
    public ApiResponse<List<NewsArticleRes.NewsArticleSummary>> getRecentNewsArticleSummaries(
            @Parameter(description = "조회할 기사 수") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<NewsArticleRes.NewsArticleSummary> result = newsArticleService.getRecentNewsArticleSummaries(limit);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("최근 뉴스 요약 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("SUMMARY_FETCH_FAILED", "최근 뉴스 요약 조회에 실패했습니다.", null);
        }
    }

    @Operation(summary = "태그별 기사 통계", description = "각 태그별 기사 개수를 조회합니다.")
    @GetMapping("/statistics/tags")
    public ApiResponse<Map<String, Long>> getNewsCountByTag() {
        try {
            Map<String, Long> result = newsArticleService.getNewsCountByTag();
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("태그별 통계 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("STATISTICS_FAILED", "태그별 통계 조회에 실패했습니다.", null);
        }
    }

    @Operation(summary = "AI 서비스 상태 확인", description = "Python AI API 서버의 상태를 확인합니다.")
    @GetMapping("/ai-health")
    public ApiResponse<Map<String, Object>> checkAIServiceHealth() {
        try {
            boolean isHealthy = newsArticleService.checkAIServiceHealth();
            Map<String, Object> result = Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "healthy", isHealthy,
                "timestamp", System.currentTimeMillis()
            );
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("AI 서비스 상태 확인 중 오류: {}", e.getMessage());
            Map<String, Object> result = Map.of(
                "status", "ERROR",
                "healthy", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ApiResponse.onSuccess(result);
        }
    }

    @Operation(summary = "단일 뉴스 기사 AI 처리", description = "단일 뉴스 기사를 AI로 분석하여 요약, 질문, 태그를 생성합니다.")
    @PostMapping("/process-single")
    public ApiResponse<NewsArticleRes.NewsArticleDetail> processSingleNewsArticle(
            @RequestBody @Valid NewsArticleReq newsArticleReq) {
        try {
            NewsArticleRes.NewsArticleDetail result = newsArticleService.processSingleNewsArticle(newsArticleReq);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("단일 뉴스 기사 처리 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("SINGLE_PROCESS_FAILED", "단일 뉴스 기사 처리에 실패했습니다: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "배치 뉴스 기사 AI 처리", description = "여러 뉴스 기사를 한번에 AI로 분석하여 요약, 질문, 태그를 생성합니다.")
    @PostMapping("/process-batch")
    public ApiResponse<List<NewsArticleRes.NewsArticleDetail>> processBatchNewsArticles(
            @RequestBody @Valid NewsArticleBatchReq batchRequest) {
        try {
            List<NewsArticleRes.NewsArticleDetail> result = newsArticleService.processBatchNewsArticles(batchRequest.getItems());
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("배치 뉴스 기사 처리 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("BATCH_PROCESS_FAILED", "배치 뉴스 기사 처리에 실패했습니다: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "뉴스 기사 채팅", description = "특정 뉴스 기사에 대해 질문하고 AI 답변을 받습니다.")
    @PostMapping("/{articleId}/chat")
    public ApiResponse<ChatRes> chatWithNewsArticle(
            @Parameter(description = "기사 ID") @PathVariable Long articleId,
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @RequestBody @Valid ChatMessageReq messageRequest) {
        try {
            ChatRes result = newsArticleService.chatWithNewsArticle(articleId, userId, messageRequest.getMessage());
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("뉴스 기사 채팅 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("CHAT_FAILED", "뉴스 기사 채팅에 실패했습니다: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "뉴스 기사 대화 내역 조회", description = "특정 사용자와 기사의 대화 내역을 조회합니다.")
    @GetMapping("/{articleId}/chat-history")
    public ApiResponse<List<ChatReq.ChatHistory>> getChatHistory(
            @Parameter(description = "기사 ID") @PathVariable Long articleId,
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {
        try {
            List<ChatReq.ChatHistory> result = newsArticleService.getChatHistory(articleId, userId);
            return ApiResponse.onSuccess(result);
        } catch (Exception e) {
            log.error("대화 내역 조회 중 오류: {}", e.getMessage());
            return ApiResponse.onFailure("HISTORY_FETCH_FAILED", "대화 내역 조회에 실패했습니다: " + e.getMessage(), null);
        }
    }
}
