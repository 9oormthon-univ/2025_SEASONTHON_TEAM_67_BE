package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.converter.NewsConverter;
import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.*;
import com.ohnew.ohnew.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;


    private News getNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    @Override
    public NewsDtoRes.NewsDetailRes getNewsDetail(Long userId, Long newsId) {
        News news = getNewsOrThrow(newsId);
        boolean scrapped = scrapRepository.existsByUserIdAndNewsId(userId, newsId);
        return NewsConverter.toDetail(news, scrapped);
    }

    @Override
    @Transactional
    public void scrap(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);
        if (scrapRepository.existsByUserIdAndNewsId(userId, newsId)) return;
        Scrap scrap = Scrap.builder().user(user).news(news).build();
        scrapRepository.save(scrap);
    }

    @Override
    @Transactional
    public void unscrap(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);
        scrapRepository.findByUserAndNews(user, news)
                .ifPresent(scrapRepository::delete);
    }

    @Override
    public List<NewsDtoRes.NewsSummaryRes> getMyScrapList(Long userId) {
        return scrapRepository.findByUserId(userId).stream()
                .map(s -> NewsConverter.toSummary(s.getNews(), true))
                .toList();
    }

    @Override
    public List<NewsDtoRes.NewsDetailRes> getTodayNews() {
        return newsRepository.findAll().stream()
                .map(s -> NewsConverter.toDetail(s, true))
                .toList();
    }
}