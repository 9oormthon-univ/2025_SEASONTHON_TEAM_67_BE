package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.dto.req.UserPreferenceDtoReq;
import com.ohnew.ohnew.dto.res.UserPreferenceDtoRes;
import com.ohnew.ohnew.entity.User;
import com.ohnew.ohnew.entity.UserPreference;
import com.ohnew.ohnew.repository.UserPreferenceRepository;
import com.ohnew.ohnew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(
                        ErrorStatus.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserPreferenceDtoRes getPreference(Long userId) {
        var pref = userPreferenceRepository.findByUserId(userId).orElse(null);
        if (pref == null) {
            // 기본값: NEUTRAL, 공백 태그
            return UserPreferenceDtoRes.builder()
                    .preferredStyle(com.ohnew.ohnew.entity.enums.NewsStyle.NEUTRAL)
                    .likedTags(java.util.Collections.emptySet())
                    .blockedTags(java.util.Collections.emptySet())
                    .build();
        }
        return UserPreferenceDtoRes.builder()
                .preferredStyle(pref.getPreferredStyle())
                .likedTags(pref.getLikedTags())
                .blockedTags(pref.getBlockedTags())
                .build();
    }

    @Transactional
    public UserPreferenceDtoRes savePreference(Long userId, UserPreferenceDtoReq req) {
        var user = getUserOrThrow(userId);
        var entity = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder()
                        .user(user)
                        .preferredStyle(com.ohnew.ohnew.entity.enums.NewsStyle.NEUTRAL)
                        .build());

        if (req.getPreferredStyle() != null) {
            entity.changeStyle(req.getPreferredStyle());
        }
        if (req.getLikedTags() != null) {
            entity.setLikedTags(req.getLikedTags());
        }
        if (req.getBlockedTags() != null) {
            entity.setBlockedTags(req.getBlockedTags());
        }// null이 온 경우 기존값 유지. "비우기"를 원한다면 빈 Set을 보내야함

        var saved = userPreferenceRepository.save(entity);    // JPA 더티체킹으로도 저장되지만 upsert명시용....
        return UserPreferenceDtoRes.builder()
                .preferredStyle(saved.getPreferredStyle())
                .likedTags(saved.getLikedTags())
                .blockedTags(saved.getBlockedTags())
                .build();
    }
}
