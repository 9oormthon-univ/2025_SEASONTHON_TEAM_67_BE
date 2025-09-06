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
        private Boolean scrapped; // 유저 기준
    }

//    @Getter
//    @Builder
//    @AllArgsConstructor
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public static class News
}
