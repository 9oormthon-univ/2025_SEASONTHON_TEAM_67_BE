package com.ohnew.ohnew.apiPayload.code.status;

import com.ohnew.ohnew.apiPayload.code.BaseErrorCode;
import com.ohnew.ohnew.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 에러 예시
    FAIL_OOOOO(HttpStatus.BAD_REQUEST, "FAIL", "실패하였습니다."),

    // 토큰 관련 에러
    JWT_FORBIDDEN(HttpStatus.FORBIDDEN, "JWT4000", "권한이 없습니다."),
    JWT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "JWT4001", "인증이 필요합니다."),
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "JWT4002", "유효하지 않은 토큰입니다."),
    JWT_EMPTY(HttpStatus.UNAUTHORIZED, "JWT4003", "JWT 토큰을 넣어주세요."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT4004", "만료된 토큰입니다."),
    JWT_REFRESHTOKEN_NOT_MATCHED(HttpStatus.UNAUTHORIZED, "JWT4005", "RefreshToken이 일치하지 않습니다."),
    JWT_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT4031", "리프레시 토큰이 존재하지 않거나 만료되었습니다."),
    JWT_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "JWT4032", "유효하지 않은 리프레시 토큰입니다."),

    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE404", "리소스를 찾을 수 없습니다."),

    //User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4004", "사용자를 찾을 수 없습니다."),
    USER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "USERINFO4004", "저장된 사용자 정보가 없습니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "USERINFO4001", "정보 수정 권한이 없습니다."),

    //로그인 관련
    EMAIL_REGISTERED_WITH_KAKAO(HttpStatus.BAD_REQUEST, "AUTH4006", "해당 이메일은 카카오 계정으로 가입되어 있습니다."),
    EMAIL_REGISTERED_WITH_LOCAL(HttpStatus.BAD_REQUEST, "AUTH4007", "이미 로컬 계정으로 가입되어 있습니다."),
    LOCAL_LOGIN_FOR_KAKAO_EMAIL(HttpStatus.UNAUTHORIZED, "AUTH4008", "카카오 계정으로 로그인해주세요."),

    // 뉴스 관련 에러
    NEWS_ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS4001", "해당 뉴스 기사를 찾을 수 없습니다."),
    RSS_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS5001", "RSS 데이터를 가져오는데 실패했습니다."),
    AI_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS5002", "AI 처리 중 오류가 발생했습니다."),

    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "VARIANT4040", "요약 변형을 찾을 수 없습니다."),

    //챗봇 관련 에러
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404", "채팅방이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
