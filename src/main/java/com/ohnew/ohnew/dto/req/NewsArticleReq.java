package com.ohnew.ohnew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticleReq {
    private String articleId;   // 기사 ID
    private String title;       // 기사 제목
    private String body;        // 기사 본문
}