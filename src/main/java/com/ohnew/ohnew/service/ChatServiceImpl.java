package com.ohnew.ohnew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.converter.ChatConverter;
import com.ohnew.ohnew.dto.req.ChatbotReq;
import com.ohnew.ohnew.dto.res.ChatDtoRes;
import com.ohnew.ohnew.entity.*;
import com.ohnew.ohnew.entity.enums.ChatSender;
import com.ohnew.ohnew.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final WebClient webClient;
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

    @Transactional
    @Override
    public ChatDtoRes.ChatTlakRes getMyChatSpecificNews(Long userId, Long newsId, String userMessage, Long chatRoomId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        String summary = Optional.ofNullable(news.getSummary()).orElse("");

        // DB에서 이전 대화 내역 가져오기
        List<ChatMessage> messages = chatMessageRepository.findByIdOrderByCreatedAtAsc(chatRoomId);
        // ChatHistory로 변환
        List<ChatbotReq.ChatHistory> history =   messages.stream()
                .map(m -> ChatbotReq.ChatHistory.builder()
                        .role(m.getSender().toString())
                        .content(m.getContent())
                        .build())
                .toList();

        ChatbotReq chatbotReq = ChatbotReq.builder()
                .articleId(newsId.toString())
                .userId(userId.toString())
                .summary(summary)
                .history(history)
                .userMessage(userMessage)
                .build();

        // 여기서 Python API 호출
        ChatDtoRes.ChatbotRes response = callPythonApi(chatbotReq);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHAT_ROOM_NOT_FOUND));

        ChatMessage saveMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(ChatSender.BOT)
                        .content(response.getAnswer())
                        .build()
        );

        // 반환값은 필요에 맞게 작성 (예: DB에 메시지 저장 후 리턴)
        return ChatConverter.toTalk(chatRoom, response.getAnswer());
    }

    private ChatDtoRes.ChatbotRes callPythonApi(ChatbotReq chatbotReq) {
        try {
            // Python API 호출
            ChatDtoRes.ChatbotRes chatDtoRes = webClient.post()
                    .uri("http://localhost:8000/py/v1/chat-article")
                    .bodyValue(chatbotReq)
                    .retrieve()
                    .bodyToMono(ChatDtoRes.ChatbotRes.class)
                    .blockOptional()
                    .orElseThrow(() -> new GeneralException(ErrorStatus.AI_PROCESSING_FAILED));

            return chatDtoRes;
        } catch (Exception e) {
            log.error("Python API 호출 실패: {}", e.getMessage(), e);
            throw new GeneralException(ErrorStatus.AI_PROCESSING_FAILED);
        }
    }
}
