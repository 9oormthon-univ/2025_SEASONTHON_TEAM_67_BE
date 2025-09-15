package com.ohnew.ohnew.service;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.dto.req.UserPreferenceDtoReq;
import com.ohnew.ohnew.entity.User;
import com.ohnew.ohnew.entity.UserPreference;
import com.ohnew.ohnew.repository.UserPreferenceRepository;
import com.ohnew.ohnew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 선호도 저장 또는 업데이트
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    // 선호도 저장 또는 업데이트
    public void saveOrUpdatePreference(Long userId, UserPreferenceDtoReq dto) {
        try {
            log.info("선호도 저장/업데이트 요청: userId={}, request={}", userId, dto);
            UserPreference userPreference = userPreferenceRepository.findByUserId(userId)
                    .orElse(null);

            if (userPreference != null) {
                // 기존 데이터 업데이트
                userPreference.setFavoriteTopics(dto.getFavoriteTopics());
                userPreference.setLessFavoriteTopics(dto.getLessFavoriteTopics());
                userPreference.setPreferredStyles(dto.getPreferredStyles());
            } else {
                // 새로 저장
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

                userPreference = UserPreference.builder()
                        .user(user)
                        .favoriteTopics(dto.getFavoriteTopics())
                        .lessFavoriteTopics(dto.getLessFavoriteTopics())
                        .preferredStyles(dto.getPreferredStyles())
                        .build();
            }

            userPreferenceRepository.save(userPreference);

        } catch (Exception e) {
            log.error("선호도 저장/업데이트 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw new GeneralException(ErrorStatus.PREFERENCE_SAVE_FAILED);
        }
    }

}