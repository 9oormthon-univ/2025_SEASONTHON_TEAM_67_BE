package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.converter.NewsConverter;
import com.ohnew.ohnew.dto.res.NewsDtoRes;
import com.ohnew.ohnew.entity.*;
import com.ohnew.ohnew.entity.enums.NewsStyle;
import com.ohnew.ohnew.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final NewsSummaryVariantRepository variantRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private static final NewsStyle DEFAULT_STYLE =
            NewsStyle.NEUTRAL;

    private News getNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private NewsStyle resolveUserStyle(Long userId) {
        // 사용자 선호 스타일 조회 (없으면 기본)
        return userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::getPreferredStyle)
                .orElse(DEFAULT_STYLE);
    }

    private boolean hasAny(Set<String> tags, Set<String> target) {
        if (tags == null || target == null) return false;
        for (String t : tags) {
            if (target.contains(t)) return true;
        }
        return false;
    }

    @Override
    public NewsDtoRes.NewsDetailRes getNewsDetail(Long userId, Long newsId) {
        User user = getUserOrThrow(userId);
        News news = getNewsOrThrow(newsId);
        boolean scrapped = scrapRepository.existsByUserIdAndNewsId(userId, newsId);

        var style = resolveUserStyle(userId); // 사용자 선호 스타일

        // 선호 스타일 변형이 없으면 NEUTRAL로 폴백
        var variant = variantRepository.findByNewsIdAndNewsStyle(newsId, style)
                .orElseGet(() -> variantRepository.findByNewsIdAndNewsStyle(newsId, DEFAULT_STYLE)
                        .orElseThrow(() -> new GeneralException(
                                ErrorStatus.VARIANT_NOT_FOUND)));

        return NewsConverter.toDetail(news, variant, scrapped);
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
        var style = resolveUserStyle(userId); // 사용자 선호 스타일
        return scrapRepository.findByUserId(userId).stream()
                .map(s -> {
                    var news = s.getNews();
                    var variant = variantRepository.findByNewsIdAndNewsStyle(news.getId(), style)
                            .orElseGet(() -> variantRepository.findByNewsIdAndNewsStyle(news.getId(), DEFAULT_STYLE)
                                    .orElseThrow(() -> new GeneralException(
                                            ErrorStatus.VARIANT_NOT_FOUND)));
                    return NewsConverter.toSummary(news, variant, true); // variant 넘김
                })
                .toList();
    }

    @Override
    public List<NewsDtoRes.NewsDetailRes> getTodayNews(Long userId) {
        getUserOrThrow(userId);

        // 사용자 선호 가져오기 (스타일/선호태그/차단태그)
        var pref = userPreferenceRepository.findByUserId(userId).orElse(null);
        var style = pref != null ? pref.getPreferredStyle() : DEFAULT_STYLE;
        var liked = pref != null ? pref.getLikedTags() : java.util.Collections.<String>emptySet();
        var blocked = pref != null ? pref.getBlockedTags() : java.util.Collections.<String>emptySet();

        // 차단 태그 포함 뉴스 배제
        var filtered = newsRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(n -> !hasAny(n.getTags(), blocked)) // 차단 태그 필터
                .toList();

        // 선호 태그 포함 뉴스 우선 정렬
        var sorted = filtered.stream()
                .sorted((a,b) -> {
                    boolean aLiked = hasAny(a.getTags(), liked);
                    boolean bLiked = hasAny(b.getTags(), liked);
                    if (aLiked == bLiked) return 0;
                    return aLiked ? -1 : 1; // 선호 포함 뉴스 먼저
                })
                .limit(20) // 20개 제한
                .toList();

        // 각 뉴스에 대해 선호 스타일 (없으면 NEUTRAL) 조회 후 변환
        return sorted.stream()
                .map(news -> {
                    boolean scrapped = scrapRepository.existsByUserIdAndNewsId(userId, news.getId());
                    var variant = variantRepository.findByNewsIdAndNewsStyle(news.getId(), style)
                            .orElseGet(() -> variantRepository.findByNewsIdAndNewsStyle(news.getId(), DEFAULT_STYLE)
                                    .orElseThrow(() -> new GeneralException(
                                            ErrorStatus.VARIANT_NOT_FOUND)));
                    return NewsConverter.toDetail(news, variant, scrapped);
                })
                .toList();
    }
}