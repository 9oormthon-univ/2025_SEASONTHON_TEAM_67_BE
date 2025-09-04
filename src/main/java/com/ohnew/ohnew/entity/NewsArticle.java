package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle extends BaseEntity { // BaseEntity : createdAt, updatedAt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String summary;

    @Column(nullable = false, unique = true)
    private String link;

    private String suggestedQuestions1;

    private String suggestedQuestions2;

    private String suggestedQuestions3;

    private String tag;

    private boolean aiProcessed = false; // AI 처리 여부
}