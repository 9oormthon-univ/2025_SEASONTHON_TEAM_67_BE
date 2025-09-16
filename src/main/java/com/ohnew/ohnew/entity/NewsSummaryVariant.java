package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.entity.enums.NewsStyle;
import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "news_summary_variant",
        uniqueConstraints = @UniqueConstraint(name="uk_news_style", columnNames = {"news_id","news_style"})
        , indexes = @Index(name="idx_variant_news", columnList="news_id")
)
public class NewsSummaryVariant extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 부모 기사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_style", nullable = false, length = 16)
    private NewsStyle newsStyle;

    private String newTitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    // 자극도
    @Column(name = "epi_stimulation_reduced")
    private String epiStimulationReduced;

    @Column(name = "epi_reason")
    private String epiReason;

    public void updateFromLLM(String title, String sum, String stimReduced, String reason) {
        this.newTitle = title;
        this.summary = sum;
        this.epiStimulationReduced = stimReduced;
        this.epiReason = reason;
    }
}