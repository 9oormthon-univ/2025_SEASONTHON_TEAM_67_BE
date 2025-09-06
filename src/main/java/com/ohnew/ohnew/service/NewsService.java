package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.News;

import java.util.List;

public interface NewsService {
    NewsDtoRes.NewsDetailRes getNewsDetail(Long userId, Long newsId);
    void scrap(Long userId, Long newsId);
    void unscrap(Long userId, Long newsId);
    List<NewsDtoRes.NewsSummaryRes> getMyScrapList(Long userId);
    List<NewsDtoRes.NewsDetailRes> getTodayNews();
}