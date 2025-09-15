package com.ohnew.ohnew.converter;

import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.News;

import java.util.ArrayList;

public class NewsConverter {

    public static NewsDtoRes.NewsSummaryRes toSummary(News n, boolean scrapped) {
        return NewsDtoRes.NewsSummaryRes.builder()
                .newsId(n.getId())
                .title(n.getTitle())
                .originalPublishedAt(n.getOriginalPublishedAt())
                .tags(new ArrayList<>(n.getTags()))
                .scrapped(scrapped)
                .build();
    }

    public static NewsDtoRes.NewsDetailRes toDetail(News n, boolean scrapped) {
        return NewsDtoRes.NewsDetailRes.builder()
                .newsId(n.getId())
                .title(n.getTitle())
                .summary(n.getSummary())
                .originalUrl(n.getOriginalUrl())
                .originalPublishedAt(n.getOriginalPublishedAt())
                .tags(new ArrayList<>(n.getTags()))
                .recommendedQuestions(n.getRecommendedQuestions())
                .scrapped(scrapped)
                .quiz(n.getQuizQuestion())
                .answer(n.getQuizAnswer())
                .build();
    }
}