package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByOriginalUrl(String url);
}