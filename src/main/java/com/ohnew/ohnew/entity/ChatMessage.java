package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.entity.enums.ChatSender;
import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 대화방 메시지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    // USER / BOT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatSender sender;

    // 메시지 본문
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
