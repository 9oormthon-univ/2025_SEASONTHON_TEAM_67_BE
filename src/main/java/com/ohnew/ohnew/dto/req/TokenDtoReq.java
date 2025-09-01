package com.ohnew.ohnew.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenDtoReq {

    @Getter
    @NoArgsConstructor
    public static class AccessTokenReq {
        @NotBlank(message = "엑세스 토큰은 필수입니다.")
        private String accessToken;
    }

    @Getter
    public static class RefreshTokenReq {
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        private String refreshToken;
    }
}
