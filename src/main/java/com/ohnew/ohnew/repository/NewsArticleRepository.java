package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    // URL 중복 체크
    boolean existsByLink(String link);

    // 아직 AI 처리되지 않은 기사 조회
    List<NewsArticle> findByAiProcessedFalse();
}
