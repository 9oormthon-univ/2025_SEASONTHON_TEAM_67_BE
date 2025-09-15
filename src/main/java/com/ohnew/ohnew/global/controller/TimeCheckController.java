package com.ohnew.ohnew.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TimeCheckController {
    @GetMapping("/_time")
    public Map<String, Object> now() {
        var now = java.time.OffsetDateTime.now();
        return java.util.Map.of(
                "appNow", now.toString(),                    // 예: 2025-09-09T23:47:42+09:00
                "zoneId", java.time.ZoneId.systemDefault().toString() // 예: Asia/Seoul
        );
    }
}