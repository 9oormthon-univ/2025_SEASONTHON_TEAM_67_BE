package com.ohnew.ohnew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotReq {

    private String articleId;
    private String userId;
    private String summary;
    private List<ChatHistory> history;
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