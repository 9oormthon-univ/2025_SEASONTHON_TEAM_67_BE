package com.ohnew.ohnew.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleRes {
    
    private List<NewsArticleDetail> processedArticles;
    private String status;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewsArticleDetail {
        private String articleId;           // 기사 ID
        private String title;               // 기사 제목
        private String summary;             // AI가 생성한 요약
        private String link;                // 원본 기사 링크
        private String suggestedQuestions1; // AI가 제안한 질문 1
        private String suggestedQuestions2; // AI가 제안한 질문 2
        private String suggestedQuestions3; // AI가 제안한 질문 3
        private String tag;                 // AI가 분류한 태그
        private LocalDateTime processedAt;  // 처리 시간
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewsArticleListRes {
        private List<NewsArticleDetail> articles;
        private int totalCount;
        private int currentPage;
        private int totalPages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewsArticleSummary {
        private Long id;
        private String title;
        private String summary;
        private String tag;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
