package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.res.ChatDtoRes;

public interface ChatService {
    ChatDtoRes.EnterChatRoomRes enterChatRoom(Long userId, Long newsId);
    ChatDtoRes.ChatMessagesRes getMyChatMessagesForNews(Long userId, Long newsId);
    ChatDtoRes.ChattedNewsListRes getMyChattedNewsList(Long userId);
    ChatDtoRes.ChatTlakRes getMyChatSpecificNews(Long userId, Long newsId, String userMessage, Long chatRoomId);
}
