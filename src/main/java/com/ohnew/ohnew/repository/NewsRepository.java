package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByOriginalUrl(String url);
    List<News> findAllByOrderByCreatedAtDesc();
}