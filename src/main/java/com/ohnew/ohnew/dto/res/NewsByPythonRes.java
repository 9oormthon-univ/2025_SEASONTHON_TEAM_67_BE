package com.ohnew.ohnew.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
        private List<Variant> variants; // 3개
        private List<String> questions; // 기사당 1세트
        private Quiz quiz;              // 기사당 1세트
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // 다른 필드가 와도 무시
    public static class Variant {
        private String newsStyle;
        private String newTitle;
        private String summary;
        private Epi epi;
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
    public static class Epi {
        private String stimulationReduced;
        private String reason;
    }

}
