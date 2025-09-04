package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
    /**
     * 특정 기사와 사용자의 대화 내역 조회 (시간순 정렬)
     */
    List<ChatHistory> findByArticleIdAndUserIdOrderByCreatedAtAsc(Long articleId, Long userId);
    
    /**
     * 특정 세션의 대화 내역 조회 (시간순 정렬)
     */
    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * 특정 기사의 모든 대화 내역 조회
     */
    List<ChatHistory> findByArticleIdOrderByCreatedAtDesc(Long articleId);
    
    /**
     * 특정 사용자의 모든 대화 내역 조회
     */
    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 특정 사용자와 기사의 최근 대화 내역 조회 (제한된 개수)
     */
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.articleId = :articleId AND ch.userId = :userId ORDER BY ch.createdAt DESC")
    List<ChatHistory> findRecentChatHistory(@Param("articleId") Long articleId, @Param("userId") Long userId);
}
