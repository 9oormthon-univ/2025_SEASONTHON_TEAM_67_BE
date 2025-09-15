package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.dto.res.KakaoUserInfoResponseDto;
import com.ohnew.ohnew.dto.res.UserDtoRes;
import com.ohnew.ohnew.service.KakaoService;
import com.ohnew.ohnew.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class SocialLoginController {

    private final KakaoService kakaoService;
    private final UserService userService;

    // 프론트 콜백형 카카오 로그인(프론트에서 리다이렉트 URI로 카카오 인가코드를 받아서 백으로 전달)
    @GetMapping("/kakao/callback")
    public ApiResponse<UserDtoRes.UserLoginRes> callback(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

        //회원가입, 로그인 동시진행
        return ApiResponse.onSuccess(userService.kakaoLoginWeb(request,response, userService.kakaoSignup(userInfo)));
    }
}
