package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle extends BaseEntity { // BaseEntity : createdAt, updatedAt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(nullable = false)
    private String suggestedQuestions1;

    @Column(nullable = false)
    private String suggestedQuestions2;

    @Column(nullable = false)
    private String suggestedQuestions3;

    @Column(nullable = false)
    private String tag;

    private boolean aiProcessed = false; // AI 처리 여부
}