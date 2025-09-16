package com.ohnew.ohnew.controller;

import com.ohnew.ohnew.apiPayload.ApiResponse;
import com.ohnew.ohnew.apiPayload.code.status.SuccessStatus;
import com.ohnew.ohnew.common.security.JwtTokenProvider;
import com.ohnew.ohnew.dto.req.TokenDtoReq;
import com.ohnew.ohnew.dto.req.UserDtoReq;
import com.ohnew.ohnew.dto.req.UserPreferenceDtoReq;
import com.ohnew.ohnew.dto.res.KakaoUserInfoResponseDto;
import com.ohnew.ohnew.dto.res.UserDtoRes;
import com.ohnew.ohnew.dto.res.UserPreferenceDtoRes;
import com.ohnew.ohnew.entity.User;
import com.ohnew.ohnew.service.KakaoService;
import com.ohnew.ohnew.service.UserPreferenceService;
import com.ohnew.ohnew.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoService kakaoService;
    private final UserPreferenceService userPreferenceService;

    @Operation(summary = "카카오로그인(앱)", description = "앱에서 '카카오 액세스 토큰'을 전달")
    @PostMapping("/kakao-login")
    public ApiResponse<UserDtoRes.UserLoginRes> kakaoLogin(@RequestBody @Valid TokenDtoReq.AccessTokenReq request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(request.getAccessToken());
        User user = userService.kakaoSignup(userInfo);
        return ApiResponse.onSuccess(userService.kakaoLogin(httpRequest, httpResponse, user));
    }


    @Operation(summary = "로그아웃(앱)", description = "액세스 토큰을 무효화하여 로그아웃")
    @PostMapping("/logout")
    public ApiResponse<SuccessStatus> logout(){
        String accessToken = jwtTokenProvider.resolveAccessToken();

        userService.logout(accessToken);
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @Operation(summary = "로컬 회원가입(앱)", description = "이메일/비밀번호로 회원가입하고 토큰 발급")
    @PostMapping("/local/signup")
    public ApiResponse<UserDtoRes.UserLoginRes> signUpLocal(
            @RequestBody @Valid UserDtoReq.SignUpReq req) {

        userService.signUpLocal(req.getEmail(), req.getPassword(), req.getName());

        // 가입 직후 자동 로그인처럼 토큰 발급
        var res = userService.loginLocal( req.getEmail(), req.getPassword());

//        // 웹: 바디에서 RT 제거
//        res.setRefreshToken(null);

        return ApiResponse.onSuccess(res);
    }

    @Operation(summary = "로컬 로그인(앱)", description = "이메일/비밀번호로 로그인하고 토큰 발급")
    @PostMapping("/local/login")
    public ApiResponse<UserDtoRes.UserLoginRes> loginLocal(
            @RequestBody @Valid UserDtoReq.LoginReq req) {

        var res = userService.loginLocal( req.getEmail(), req.getPassword());

//        // 웹: 바디에서 RT 제거 (쿠키만 사용)
//        res.setRefreshToken(null);

        return ApiResponse.onSuccess(res);
    }

//    @Operation(summary = "웹용-로그아웃 API", description = "액세스 토큰을 무효화하고 쿠키 삭제")
//    @PostMapping("/logout")
//    public ApiResponse<String> logoutWeb(
//            @RequestHeader(value = "Authorization", required = false) String accessToken,
//            HttpServletRequest request, HttpServletResponse response) {
//
//        userService.logoutWeb(request, response, accessToken);
//        return ApiResponse.onSuccess("로그아웃 성공이요~");
//    }


    @Operation(summary = "토큰 재발급(앱)", description = "리프레시 토큰을 사용하여 새로운 액세스/리프레시 토큰 발급")
    @PostMapping("/refresh")
    public ApiResponse<UserDtoRes.UserLoginRes> refresh() {
        // 앱은 헤더로 리플레시 토큰 확인, 액세스 토큰은 확인 x
        String refreshToken = jwtTokenProvider.resolveRefreshToken();

        var res = userService.rotateTokensForApp(refreshToken);

        return ApiResponse.onSuccess(res);
    }
    @Operation(summary = "내 뉴스 선호 조회", description = "선호 스타일/선호 태그/차단 태그")
    @GetMapping("/preferences/news")
    public ApiResponse<UserPreferenceDtoRes> getNewsPreference() {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(userPreferenceService.getPreference(userId));
    }

    @Operation(summary = "내 뉴스 선호 저장", description = "선호 스타일 및 태그를 저장(덮어쓰기)")
    @PutMapping("/preferences/news")
    public ApiResponse<UserPreferenceDtoRes> saveNewsPreference(@RequestBody UserPreferenceDtoReq req) {
        Long userId = jwtTokenProvider.getUserIdFromToken();
        return ApiResponse.onSuccess(userPreferenceService.savePreference(userId, req));
    }

}
