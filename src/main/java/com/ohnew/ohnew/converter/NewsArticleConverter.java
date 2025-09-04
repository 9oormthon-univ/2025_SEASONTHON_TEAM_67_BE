package com.ohnew.ohnew.converter;

import com.ohnew.ohnew.dto.res.NewsArticleRes;
import com.ohnew.ohnew.entity.NewsArticle;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class NewsArticleConverter {

    /**
     * NewsArticle Entity -> NewsArticleDetail DTO 변환
     */
    public static NewsArticleRes.NewsArticleDetail toNewsArticleDetail(NewsArticle newsArticle) {
        return NewsArticleRes.NewsArticleDetail.builder()
                .articleId(String.valueOf(newsArticle.getId()))
                .title(newsArticle.getTitle())
                .summary(newsArticle.getSummary())
                .link(newsArticle.getLink())
                .suggestedQuestions1(newsArticle.getSuggestedQuestions1())
                .suggestedQuestions2(newsArticle.getSuggestedQuestions2())
                .suggestedQuestions3(newsArticle.getSuggestedQuestions3())
                .tag(newsArticle.getTag())
                .processedAt(newsArticle.getCreatedAt())
                .build();
    }

    /**
     * AI 처리 결과 -> NewsArticle Entity 변환
     */
    public static NewsArticle toNewsArticleEntity(NewsArticleRes.NewsArticleDetail aiResult, String originalLink) {
        return NewsArticle.builder()
                .title(aiResult.getTitle())
                .summary(aiResult.getSummary())
                .link(originalLink != null ? originalLink : aiResult.getLink())
                .suggestedQuestions1(aiResult.getSuggestedQuestions1())
                .suggestedQuestions2(aiResult.getSuggestedQuestions2())
                .suggestedQuestions3(aiResult.getSuggestedQuestions3())
                .tag(aiResult.getTag())
                .build();
    }

    /**
     * NewsArticle Entity List -> NewsArticleDetail DTO List 변환
     */
    public static List<NewsArticleRes.NewsArticleDetail> toNewsArticleDetailList(List<NewsArticle> newsArticles) {
        return newsArticles.stream()
                .map(NewsArticleConverter::toNewsArticleDetail)
                .collect(Collectors.toList());
    }

    /**
     * NewsArticle Page -> NewsArticleListRes DTO 변환
     */
    public static NewsArticleRes.NewsArticleListRes toNewsArticleListRes(Page<NewsArticle> newsArticlePage) {
        List<NewsArticleRes.NewsArticleDetail> articles = newsArticlePage.getContent().stream()
                .map(NewsArticleConverter::toNewsArticleDetail)
                .collect(Collectors.toList());

        return NewsArticleRes.NewsArticleListRes.builder()
                .articles(articles)
                .totalCount((int) newsArticlePage.getTotalElements())
                .currentPage(newsArticlePage.getNumber())
                .totalPages(newsArticlePage.getTotalPages())
                .build();
    }

    /**
     * NewsArticle Entity -> NewsArticleSummary DTO 변환
     */
    public static NewsArticleRes.NewsArticleSummary toNewsArticleSummary(NewsArticle newsArticle) {
        return NewsArticleRes.NewsArticleSummary.builder()
                .id(newsArticle.getId())
                .title(newsArticle.getTitle())
                .summary(newsArticle.getSummary())
                .tag(newsArticle.getTag())
                .createdAt(newsArticle.getCreatedAt())
                .updatedAt(newsArticle.getUpdatedAt())
                .build();
    }

    /**
     * NewsArticle Entity List -> NewsArticleSummary DTO List 변환
     */
    public static List<NewsArticleRes.NewsArticleSummary> toNewsArticleSummaryList(List<NewsArticle> newsArticles) {
        return newsArticles.stream()
                .map(NewsArticleConverter::toNewsArticleSummary)
                .collect(Collectors.toList());
    }
}
