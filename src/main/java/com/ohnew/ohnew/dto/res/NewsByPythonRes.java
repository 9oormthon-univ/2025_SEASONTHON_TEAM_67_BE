package com.ohnew.ohnew.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsByPythonRes {
    
    private List<PythonRes> results;
    private String status;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PythonRes {
        private String articleId;           // 기사 ID
        private Boolean ok;
        private NewsData data;
        private String error;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsData {
        private String articleId;
        private String newsTitle;
        private String summary;
        private List<String> questions;
        private Quiz quiz;
        private AiToken tokensUsed;
        private String model;
        private Long latencyMs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Quiz {
        private String question;
        private String answer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiToken {
        private Long input;
        private Long output;
    }


}
