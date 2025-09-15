package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.common.security.JwtTokenProvider;
import com.ohnew.ohnew.dto.req.ChatbotDtoReq;
import com.ohnew.ohnew.dto.res.ChatDtoRes;
import com.ohnew.ohnew.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "특정 기사 챗봇 대화방 입장/생성", description = "추천 질문 포함 반환")
    @PostMapping("/rooms/enter")
    public ApiResponse<ChatDtoRes.EnterChatRoomRes> enterChatRoom(@RequestParam Long newsId) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(chatService.enterChatRoom(userId, newsId));
    }

    @Operation(summary = "로그인 사용자의 채팅 기록이 있는 뉴스 리스트 조회", description = "최근 대화 순 정렬")
    @GetMapping("/news")
    public ApiResponse<ChatDtoRes.ChattedNewsListRes> myChattedNews() {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(chatService.getMyChattedNewsList(userId));
    }

    @Operation(summary = "로그인 사용자의 특정 기사 채팅 기록 조회", description = "오름차순 정렬")
    @GetMapping("/news/{newsId}/messages")
    public ApiResponse<ChatDtoRes.ChatMessagesRes> myChatMessagesForNews(@PathVariable Long newsId) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(chatService.getMyChatMessagesForNews(userId, newsId));
    }

    @Operation(summary = "특정 기사로 챗봇이랑 대화하기", description = "특정 기사를 포함한 대화")
    @PostMapping("/news/{newsId}/talk")
    public ApiResponse<ChatDtoRes.ChatTlakRes> specificNewsChat(@PathVariable Long newsId, @RequestBody ChatbotDtoReq.ChatMessageRes chatbotReq) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(chatService.getMyChatSpecificNews(userId, newsId, chatbotReq.getMessage(), chatbotReq.getChatRoomId()));
    }
}