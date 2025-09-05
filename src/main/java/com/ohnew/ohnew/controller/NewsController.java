package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.common.security.JwtTokenProvider;
import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "특정 기사 조회", description = "추천질문/태그/스크랩 여부 포함")
    @GetMapping("/{newsId}")
    public ApiResponse<NewsDtoRes.NewsDetailRes> getNews(@PathVariable Long newsId) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(newsService.getNewsDetail(userId, newsId));
    }

    @Operation(summary = "뉴스 스크럽(즐겨찾기)", description = "이미 스크럽되어 있으면 무시")
    @PostMapping("/{newsId}/scrap")
    public ApiResponse<String> scrap(@PathVariable Long newsId) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        newsService.scrap(userId, newsId);
        return ApiResponse.onSuccess("스크럽 완료");
    }

    @Operation(summary = "뉴스 스크럽 해제", description = "스크럽이 없으면 무시")
    @DeleteMapping("/{newsId}/scrap")
    public ApiResponse<String> unscrap(@PathVariable Long newsId) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        newsService.unscrap(userId, newsId);
        return ApiResponse.onSuccess("스크럽 해제 완료");
    }

    @Operation(summary = "내 스크럽 뉴스 리스트 조회", description = "간단 요약 목록")
    @GetMapping("/scraps/me")
    public ApiResponse<List<NewsDtoRes.NewsSummaryRes>> myScraps() {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(newsService.getMyScrapList(userId));
    }

}