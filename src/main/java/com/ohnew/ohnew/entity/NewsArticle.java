package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle extends BaseEntity { // BaseEntity : createdAt, updatedAt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false, unique = true, length = 1000)
    private String link;

    @Column(length = 500)
    private String suggestedQuestions1;

    @Column(length = 500)
    private String suggestedQuestions2;

    @Column(length = 500)
    private String suggestedQuestions3;

    @Column(length = 100)
    private String tag;
}