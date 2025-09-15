package com.ohnew.ohnew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotDtoReq {

    private String articleId;
    private String userId;
    private String summary;
    @Builder.Default
    private List<ChatHistory> history = new ArrayList<>();
    private String userMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistory {
        private String role;     // "user" 또는 "assistant"
        private String content;  // 대화 내용
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRes{
        private String message;
        private Long chatRoomId;
    }
}