package com.ohnew.ohnew.apiPayload.code.status;

import com.ohnew.ohnew.apiPayload.code.BaseErrorCode;
import com.ohnew.ohnew.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NewsErrorStatus implements BaseErrorCode {

    // 뉴스 관련 에러
    NEWS_ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS4001", "해당 뉴스 기사를 찾을 수 없습니다."),
    RSS_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS5001", "RSS 데이터를 가져오는데 실패했습니다."),
    AI_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS5002", "AI 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
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
