package com.ohnew.ohnew.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ohnew.ohnew.entity.enums.ChatSender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDtoRes {

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnterChatRoomRes {
        private Long chatRoomId;
        private Long newsId;
        private List<String> recommendedQuestions; // 챗봇 시작용
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ChatMessageItem {
        private Long messageId;
        private ChatSender sender;
        private String content;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ChatMessagesRes {
        private Long chatRoomId;
        private Long newsId;
        private List<ChatMessageItem> messages;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ChattedNewsItem {
        private Long newsId;
        private String title;
        private LocalDateTime lastMessageAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ChattedNewsListRes {
        private List<ChattedNewsItem> items;
    }
}