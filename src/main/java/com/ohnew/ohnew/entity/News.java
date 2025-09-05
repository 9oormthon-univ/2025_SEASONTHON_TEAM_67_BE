package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI가 생성한 기사 제목
    private String title;

    // AI가 요약한 기사 본문
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 원본 기사 URL
    private String originalUrl;

    // 원본 기사 생성(발행) 날짜
    private LocalDate originalPublishedAt;

    // 기사 태그 (간단 버전: 문자열 Set)
    @ElementCollection
    @CollectionTable(name = "news_tags", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "tag")
    private Set<String> tags;

    // AI가 생성한 추천 질문(최대 4개 예상) - 간단 리스트로 저장
    @ElementCollection
    @CollectionTable(name = "news_recommended_questions", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "question")
    private List<String> recommendedQuestions;
}
