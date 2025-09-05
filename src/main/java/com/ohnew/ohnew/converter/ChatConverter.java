package com.ohnew.ohnew.converter;

import com.ohnew.ohnew.dto.res.ChatDtoRes;
import com.ohnew.ohnew.entity.ChatMessage;
import com.ohnew.ohnew.entity.ChatRoom;

import java.util.List;

public class ChatConverter {

    public static ChatDtoRes.EnterChatRoomRes toEnter(ChatRoom room) {
        return ChatDtoRes.EnterChatRoomRes.builder()
                .chatRoomId(room.getId())
                .newsId(room.getNews().getId())
                .recommendedQuestions(room.getNews().getRecommendedQuestions())
                .build();
    }

    public static ChatDtoRes.ChatMessagesRes toMessages(ChatRoom room, List<ChatMessage> messages) {
        return ChatDtoRes.ChatMessagesRes.builder()
                .chatRoomId(room.getId())
                .newsId(room.getNews().getId())
                .messages(messages.stream()
                        .map(m -> new ChatDtoRes.ChatMessageItem(
                                m.getId(), m.getSender(), m.getContent(), m.getCreatedAt()
                        )).toList())
                .build();
    }
}