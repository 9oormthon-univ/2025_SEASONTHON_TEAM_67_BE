package com.ohnew.ohnew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDtoReq {

    // 자주 보고 싶은 주제
    private List<String> favoriteTopics;

    // 적게 보고 싶은 주제
    private List<String> lessFavoriteTopics;

    // 보고 싶은 문체 선택
    private String preferredStyles;
}
