package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    /**
     * 링크로 기사 조회 (중복 방지)
     */
    Optional<NewsArticle> findByLink(String link);
    
    /**
     * 태그로 기사 조회
     */
    List<NewsArticle> findByTag(String tag);
    
    /**
     * 태그로 기사 페이징 조회
     */
    Page<NewsArticle> findByTag(String tag, Pageable pageable);
    
    /**
     * 제목으로 기사 검색
     */
    @Query("SELECT n FROM NewsArticle n WHERE n.title LIKE %:keyword%")
    List<NewsArticle> findByTitleContaining(@Param("keyword") String keyword);
    
    /**
     * 제목 또는 요약에서 키워드 검색
     */
    @Query("SELECT n FROM NewsArticle n WHERE n.title LIKE %:keyword% OR n.summary LIKE %:keyword%")
    List<NewsArticle> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 제목 또는 요약에서 키워드 검색 (페이징)
     */
    @Query("SELECT n FROM NewsArticle n WHERE n.title LIKE %:keyword% OR n.summary LIKE %:keyword%")
    Page<NewsArticle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 특정 시간 이후 생성된 기사 조회
     */
    List<NewsArticle> findByCreatedAtAfter(LocalDateTime dateTime);
    
    /**
     * 최신 기사 조회 (페이징)
     */
    Page<NewsArticle> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 태그별 기사 개수 조회
     */
    @Query("SELECT n.tag, COUNT(n) FROM NewsArticle n GROUP BY n.tag")
    List<Object[]> countByTag();
    
    /**
     * 링크 존재 여부 확인
     */
    boolean existsByLink(String link);
}
