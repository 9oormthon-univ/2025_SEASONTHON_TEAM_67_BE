package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.ChatMessage;
import com.ohnew.ohnew.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    ChatMessage findTop1ByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
    List<ChatMessage> findByIdOrderByCreatedAtAsc(Long chatRoomId);
}