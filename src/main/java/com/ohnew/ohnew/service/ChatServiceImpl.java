package com.ohnew.ohnew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.converter.ChatConverter;
import com.ohnew.ohnew.dto.req.ChatbotReq;
import com.ohnew.ohnew.dto.res.ChatDtoRes;
import com.ohnew.ohnew.entity.*;
import com.ohnew.ohnew.entity.enums.ChatSender;
import com.ohnew.ohnew.entity.enums.NewsStyle;
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
    private final NewsSummaryVariantRepository variantRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private News getNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private NewsStyle resolveUserStyle(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::getPreferredStyle)
                .orElse(NewsStyle.NEUTRAL);
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

        var style = resolveUserStyle(userId);
        var items = rooms.stream().map(r -> {
                    var last = chatMessageRepository.findTop1ByChatRoomOrderByCreatedAtDesc(r);

                    // 제목은 Variant의 newTitle 사용
                    var news = r.getNews();
                    var variant = variantRepository.findByNewsIdAndNewsStyle(news.getId(), style)
                            .orElseGet(() -> variantRepository.findByNewsIdAndNewsStyle(news.getId(),
                                            com.ohnew.ohnew.entity.enums.NewsStyle.NEUTRAL)
                                    .orElse(null));

                    String title = (variant != null && variant.getNewTitle()!=null)
                            ? variant.getNewTitle() : "(제목 준비중)";

                    return new ChatDtoRes.ChattedNewsItem(
                            news.getId(),
                            title,                                   // Variant 기반 제목
                            last != null ? last.getCreatedAt() : r.getCreatedAt()
                    );
                }).sorted(Comparator.comparing(ChatDtoRes.ChattedNewsItem::getLastMessageAt).reversed())
                .toList();

        return ChatDtoRes.ChattedNewsListRes.builder().items(items).build();
    }

    public ChatDtoRes.ChatTlakRes getMyChatSpecificNews(Long userId, Long newsId, String userMessage) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);

        var style = resolveUserStyle(userId);
        var variant = variantRepository.findByNewsIdAndNewsStyle(newsId, style)
                .orElseGet(() -> variantRepository.findByNewsIdAndNewsStyle(newsId,
                                com.ohnew.ohnew.entity.enums.NewsStyle.NEUTRAL)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.VARIANT_NOT_FOUND )));

        String summary = variant.getSummary();

        // 채팅방 로드
        ChatRoom chatRoom = chatRoomRepository.findByUserAndNews(user, news)
                .orElseGet(() -> chatRoomRepository.save(
                        ChatRoom.builder().user(user).news(news).build()
                ));

        // 이전 대화 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

        // 이번 사용자 발화 저장
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(ChatSender.USER)
                        .content(userMessage)
                        .build()
        );

        // history 변환
        List<ChatbotReq.ChatHistory> history = messages.stream()
                .map(m -> ChatbotReq.ChatHistory.builder()
                        .role(m.getSender() == ChatSender.BOT ? "assistant" : "user")
                        .content(m.getContent())
                        .build())
                .toList();

        // 파이썬 요청 바디
        ChatbotReq chatbotReq = ChatbotReq.builder()
                .articleId(newsId.toString())  // 부모 ID
                .userId(userId.toString())
                .summary(summary)              // Variant 요약
                .history(history)
                .userMessage(userMessage)
                .build();

        var response = callPythonApi(chatbotReq);

        // 봇 응답 저장
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(ChatSender.BOT)
                        .content(response.getAnswer())
                        .build()
        );

        return ChatConverter.toTalk(chatRoom, response.getAnswer());
    }
}
