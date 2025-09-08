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
@Transactional
public class ChatServiceImpl implements ChatService {

    private final WebClient webClient;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private News getNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private ChatDtoRes.ChatbotRes callPythonApi(ChatbotReq chatbotReq) {
        try {
            // 요청 DTO 로깅 (JSON 직렬화)
            String reqJson = objectMapper.writeValueAsString(chatbotReq);
            log.info("Python API 요청: {}", reqJson);

            // Python API 호출
            ChatDtoRes.ChatbotRes chatDtoRes = webClient.post()
                    .uri("http://localhost:8000/v1/chat-article")
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

    public ChatDtoRes.EnterChatRoomRes enterChatRoom(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        ChatRoom room = chatRoomRepository.findByUserAndNews(user, news)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder().user(user).news(news).build()
                ));

        return ChatConverter.toEnter(room);
    }

    @Transactional(readOnly = true)
    public ChatDtoRes.ChatMessagesRes getMyChatMessagesForNews(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        ChatRoom room = chatRoomRepository.findByUserAndNews(user, news)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));

        var messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room);
        return ChatConverter.toMessages(room, messages);
    }

    @Transactional(readOnly = true)
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

    public ChatDtoRes.ChatTlakRes getMyChatSpecificNews(Long userId, Long newsId, String userMessage, Long chatRoomId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);
        String summary = Optional.ofNullable(news.getSummary()).orElse("");

        // 채팅방 로드
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHAT_ROOM_NOT_FOUND));

        // 이전 대화 조회 (채팅방 기준, 생성시간 오름차순)
        List<ChatMessage> messages =
                chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        // 이번 사용자 발화 저장 (history에는 포함되지 않도록 '조회 후' 저장)
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(ChatSender.USER)
                        .content(userMessage)
                        .build()
        );

        // history 변환 (이전 대화만)
        List<ChatbotReq.ChatHistory> history = messages.stream()
                .map(m -> ChatbotReq.ChatHistory.builder()
                        .role(m.getSender() == ChatSender.BOT ? "assistant" : "user")
                        .content(m.getContent())
                        .build())
                .toList();

        // 파이썬 요청 바디
        ChatbotReq chatbotReq = ChatbotReq.builder()
                .articleId(newsId.toString())
                .userId(userId.toString())
                .summary(summary)
                .history(history)        // 이전 대화
                .userMessage(userMessage) // 이번 사용자 발화
                .build();

        // 파이썬 호출
        ChatDtoRes.ChatbotRes response = callPythonApi(chatbotReq);

        // 봇 응답 저장
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(ChatSender.BOT)
                        .content(response.getAnswer())
                        .build()
        );

        // 반환
        return ChatConverter.toTalk(chatRoom, response.getAnswer());
    }


}
