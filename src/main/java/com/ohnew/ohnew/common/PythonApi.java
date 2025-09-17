package com.ohnew.ohnew.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class PythonApi {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T callPythonApi(String uri, Object request, Class<T> responseType) {
        try {
            // 요청 DTO 로깅 (JSON 직렬화)
            String reqJson = objectMapper.writeValueAsString(request);
            log.info("Python API 요청 [{}]: {}", uri, reqJson);

            // Python API 호출
            return webClient.post()
                    .uri(uri)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(responseType)
                    .blockOptional()
                    .orElseThrow(() -> new GeneralException(ErrorStatus.AI_PROCESSING_FAILED));
        } catch (Exception e) {
            log.error("Python API 호출 실패 [{}]: {}", uri, e.getMessage(), e);
            throw new GeneralException(ErrorStatus.AI_PROCESSING_FAILED);
        }
    }
}