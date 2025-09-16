package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 원본 기사 URL
    @Column(unique = true,  nullable = false)
    private String originalUrl;

    // 원본 기사 생성(발행) 날짜
    private LocalDate originalPublishedAt;

    // 기사 태그 (간단 버전: 문자열 Set)
    @ElementCollection
    @CollectionTable(name = "news_tags", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new java.util.HashSet<>();

    // AI가 생성한 추천 질문
    @ElementCollection
    @CollectionTable(name = "news_recommended_questions", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "question")
    @Builder.Default
    private List<String> recommendedQuestions= new java.util.ArrayList<>();

    @Column(name = "quiz_question")
    private String quizQuestion;

    @Column(name = "quiz_answer")
    private String quizAnswer; //YES,NO -> enum으로 바꿀 예정

    // --- 도메인 메서드 ---
    public void replaceQuestions(java.util.List<String> questions) {
        this.recommendedQuestions.clear();
        if (questions == null) return;

        java.util.LinkedHashSet<String> normalized = questions.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        this.recommendedQuestions.addAll(normalized.stream().limit(4).toList());
    }

    public void setQuiz(String question, String answer) {
        String q = (question == null) ? null : question.trim();
        String a = (answer == null) ? null : answer.trim();

        // 빈문자면 null 처리 (DB nullable)
        this.quizQuestion = (q == null || q.isEmpty()) ? null : q;
        this.quizAnswer   = (a == null || a.isEmpty()) ? null : a;
    }

    public void clearQuiz() {
        this.quizQuestion = null;
        this.quizAnswer = null;
    }
}
