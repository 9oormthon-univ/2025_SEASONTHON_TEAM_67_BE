package com.ohnew.ohnew.service;

import com.ohnew.ohnew.dto.req.UserDtoReq;
import com.ohnew.ohnew.dto.res.KakaoUserInfoResponseDto;
import com.ohnew.ohnew.dto.res.UserDtoRes;
import com.ohnew.ohnew.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserDtoRes.UserLoginRes loginLocal(String email, String rawPassword);
    void signUpLocal(String email, String rawPassword, String name);
    UserDtoRes.UserLoginRes loginLocalWeb(HttpServletRequest request, HttpServletResponse response, String email, String rawPassword);
    UserDtoRes.UserLoginRes login(HttpServletRequest request, HttpServletResponse response, UserDtoReq.LoginReq loginDto);
    void logout(String accessToken);
    void logoutWeb(HttpServletRequest request, HttpServletResponse response, String accessToken);
    User kakaoSignup(KakaoUserInfoResponseDto userInfo);
    UserDtoRes.UserLoginRes kakaoLogin(HttpServletRequest request, HttpServletResponse response, User user);
    UserDtoRes.UserLoginRes kakaoLoginWeb(HttpServletRequest request, HttpServletResponse response, User user);
    UserDtoRes.UserLoginRes rotateTokensForApp(String refreshToken);
}
