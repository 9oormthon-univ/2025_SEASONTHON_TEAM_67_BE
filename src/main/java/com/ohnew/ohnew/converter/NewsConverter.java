package com.ohnew.ohnew.converter;

import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.News;
import com.ohnew.ohnew.entity.NewsSummaryVariant;

import java.util.ArrayList;

public class NewsConverter {

    public static NewsDtoRes.NewsSummaryRes toSummary(News n, NewsSummaryVariant v, boolean scrapped) {
        return NewsDtoRes.NewsSummaryRes.builder()
                .newsId(n.getId())
                .title(v.getNewTitle())
                .originalPublishedAt(n.getOriginalPublishedAt())
                .tags(new ArrayList<>(n.getTags()))
                .scrapped(scrapped)
                .build();
    }

    public static NewsDtoRes.NewsDetailRes toDetail(News n, NewsSummaryVariant v, boolean scrapped) {
        return NewsDtoRes.NewsDetailRes.builder()
                .newsId(n.getId())
                .title(v.getNewTitle())
                .summary(v.getSummary())
                .originalUrl(n.getOriginalUrl())
                .originalPublishedAt(n.getOriginalPublishedAt())
                .tags(new ArrayList<>(n.getTags()))
                .recommendedQuestions(n.getRecommendedQuestions())
                .quiz(n.getQuizQuestion())
                .answer(n.getQuizAnswer())
                .scrapped(scrapped)
                .newsStyle(v.getNewsStyle().name())
                .epiStimulationReduced(v.getEpiStimulationReduced())
                .epiReason(v.getEpiReason())
                .build();
    }
}