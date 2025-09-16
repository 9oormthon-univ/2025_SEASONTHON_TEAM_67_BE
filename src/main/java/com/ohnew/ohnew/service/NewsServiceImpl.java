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

import java.util.*;
import java.util.stream.Collectors;

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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // 화면에 노출 가능한지(요약/퀴즈 필수) 판별
    private static boolean isDisplayable(News n, NewsSummaryVariant v) {
        // 요약 필수
        if (v == null || isBlank(v.getSummary())) return false;
        // 퀴즈(문항/정답) 필수
        if (isBlank(n.getQuizQuestion()) || isBlank(n.getQuizAnswer())) return false;
        return true;
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

        // 선호 불러오기
        var pref    = userPreferenceRepository.findByUserId(userId).orElse(null);
        var style   = (pref != null) ? pref.getPreferredStyle() : DEFAULT_STYLE;
        var liked   = (pref != null) ? pref.getLikedTags()       : Collections.<String>emptySet();
        var blocked = (pref != null) ? pref.getBlockedTags()     : Collections.<String>emptySet();

        // 차단 태그 제외
        var base = newsRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(n -> !hasAny(n.getTags(), blocked))
                .collect(Collectors.toList());

        // 선호 태그 우선 + 최신순 보조키
        base.sort(Comparator
                .comparing((News n) -> hasAny(n.getTags(), liked)).reversed()
                .thenComparing(News::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        // 순회하면서 displayable 한 것만 담고, 20개 차면 stop
        List<NewsDtoRes.NewsDetailRes> out = new ArrayList<>(20);
        for (News n : base) {
            // 선호 스타일 -> NEUTRAL 폴백 (없으면 skip)
            var variantOpt = variantRepository.findByNewsIdAndNewsStyle(n.getId(), style)
                    .or(() -> variantRepository.findByNewsIdAndNewsStyle(n.getId(), DEFAULT_STYLE));
            if (variantOpt.isEmpty()) continue; // 예외 대신 skip
            var v = variantOpt.get();

            if (!isDisplayable(n, v)) continue; // 요약/퀴즈 null이면 skip

            boolean scrapped = scrapRepository.existsByUserIdAndNewsId(userId, n.getId());
            out.add(NewsConverter.toDetail(n, v, scrapped));

            if (out.size() >= 20) break; // 필터 후 제한
        }

        return out;
    }
}