package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_histories")
public class ChatHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long articleId;     // 기사 ID

    @Column(nullable = false)
    private Long userId;        // 사용자 ID

    @Column(nullable = false)
    private String role;        // "user" 또는 "assistant"

    @Column(nullable = false, length = 2000)
    private String content;     // 대화 내용

    @Column(nullable = false)
    private String sessionId;   // 대화 세션 ID (같은 대화 세션 구분용)
}
