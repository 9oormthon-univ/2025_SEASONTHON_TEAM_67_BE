package com.ohnew.ohnew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReq {
    private String articleId;     // 기사 ID (DB에 저장된 기사 Id)
    private String userId;        // 사용자 ID
    private String summary;       // DB에 저장된 기사 요약 내용
    private List<ChatHistory> history; // 해당 사용자가 해당 기사에 대해 이야기한 대화 내역
    private String userMessage;   // 현재 사용자가 입력한 채팅

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatHistory {
        private String role;      // "user" 또는 "assistant"
        private String content;   // 대화 내용
    }
}
