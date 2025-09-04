package com.ohnew.ohnew.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRes {
    private String answer;      // AI 답변
    private String articleId;   // 관련 기사 ID
    private boolean success;    // 성공 여부
    private String message;     // 메시지
}
