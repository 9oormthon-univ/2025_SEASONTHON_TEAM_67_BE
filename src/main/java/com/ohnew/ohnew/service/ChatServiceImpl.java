package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.converter.ChatConverter;
import com.ohnew.ohnew.dto.res.ChatDtoRes;
import com.ohnew.ohnew.entity.*;
import com.ohnew.ohnew.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    private News getNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public ChatDtoRes.EnterChatRoomRes enterChatRoom(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        ChatRoom room = chatRoomRepository.findByUserAndNews(user, news)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder().user(user).news(news).build()
                ));

        return ChatConverter.toEnter(room);
    }

    @Override
    public ChatDtoRes.ChatMessagesRes getMyChatMessagesForNews(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        ChatRoom room = chatRoomRepository.findByUserAndNews(user, news)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        var messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room);
        return ChatConverter.toMessages(room, messages);
    }

    @Override
    public ChatDtoRes.ChattedNewsListRes getMyChattedNewsList(Long userId) {
        var rooms = chatRoomRepository.findByUserId(userId);
        var items = rooms.stream().map(r -> {
                    var last = chatMessageRepository.findTop1ByChatRoomOrderByCreatedAtDesc(r);
                    return new ChatDtoRes.ChattedNewsItem(
                            r.getNews().getId(),
                            r.getNews().getTitle(),
                            last != null ? last.getCreatedAt() : r.getCreatedAt()
                    );
                }).sorted(Comparator.comparing(ChatDtoRes.ChattedNewsItem::getLastMessageAt).reversed())
                .toList();

        return ChatDtoRes.ChattedNewsListRes.builder().items(items).build();
    }
}