package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.News;
import com.ohnew.ohnew.entity.Scrap;
import com.ohnew.ohnew.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByUserAndNews(User user, News news);
    List<Scrap> findByUserId(Long userId);
    boolean existsByUserIdAndNewsId(Long userId, Long newsId);
}