package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.NewsSummaryVariant;
import com.ohnew.ohnew.entity.enums.NewsStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsSummaryVariantRepository extends JpaRepository<NewsSummaryVariant, Long> {
    Optional<NewsSummaryVariant> findByNewsIdAndNewsStyle(Long newsId, NewsStyle style);
    List<NewsSummaryVariant> findAllByNewsId(Long newsId);
}
