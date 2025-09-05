package com.ohnew.ohnew.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RssNewsRes {
    private String articleId;
    private String title;
    private String body;
}
