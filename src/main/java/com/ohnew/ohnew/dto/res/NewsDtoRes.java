package com.ohnew.ohnew.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class NewsDtoRes {

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NewsSummaryRes {
        private Long newsId;
        private String title;
        private LocalDate originalPublishedAt;
        private List<String> tags;
        private Boolean scrapped; // 유저 기준
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NewsDetailRes {
        private Long newsId;
        private String title;
        private String summary;
        private String originalUrl;
        private LocalDate originalPublishedAt;
        private List<String> tags;
        private List<String> recommendedQuestions;
        private String quiz;
        private String answer;
        private Boolean scrapped; // 유저 기준
        private String newsStyle;
        private String epiStimulationReduced; // 예: "자극도를 34% 줄였어요"
        private String epiReason; // 예: "과장 어휘/감정 표현 축소"
    }

//    @Getter
//    @Builder
//    @AllArgsConstructor
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class News
}
