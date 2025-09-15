package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.common.security.JwtTokenProvider;
import com.ohnew.ohnew.dto.req.UserPreferenceDtoReq;
import com.ohnew.ohnew.entity.UserPreference;
import com.ohnew.ohnew.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/onboarding")
    public ApiResponse<String> saveOrUpdatePreference(@RequestBody UserPreferenceDtoReq userPreferenceDtoReq) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        userPreferenceService.saveOrUpdatePreference(userId, userPreferenceDtoReq);
        return ApiResponse.onSuccess("선호도 저장완료.");
    }

}
