package com.ohnew.ohnew.converter;

import com.ohnew.ohnew.dto.req.ChatReq;
import com.ohnew.ohnew.entity.ChatHistory;

import java.util.List;
import java.util.stream.Collectors;

public class ChatHistoryConverter {

    /**
     * ChatHistory Entity -> ChatReq.ChatHistory DTO 변환
     */
    public static ChatReq.ChatHistory toChatHistoryDto(ChatHistory chatHistory) {
        return ChatReq.ChatHistory.builder()
                .role(chatHistory.getRole())
                .content(chatHistory.getContent())
                .build();
    }

    /**
     * ChatHistory Entity List -> ChatReq.ChatHistory DTO List 변환
     */
    public static List<ChatReq.ChatHistory> toChatHistoryDtoList(List<ChatHistory> chatHistories) {
        return chatHistories.stream()
                .map(ChatHistoryConverter::toChatHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 메시지를 ChatHistory Entity로 변환
     */
    public static ChatHistory toUserChatHistoryEntity(Long articleId, Long userId, String content, String sessionId) {
        return ChatHistory.builder()
                .articleId(articleId)
                .userId(userId)
                .role("user")
                .content(content)
                .sessionId(sessionId)
                .build();
    }

    /**
     * AI 응답을 ChatHistory Entity로 변환
     */
    public static ChatHistory toAssistantChatHistoryEntity(Long articleId, Long userId, String content, String sessionId) {
        return ChatHistory.builder()
                .articleId(articleId)
                .userId(userId)
                .role("assistant")
                .content(content)
                .sessionId(sessionId)
                .build();
    }
}
