package com.ohnew.ohnew.dto.req;

import com.ohnew.ohnew.entity.enums.NewsStyle;
import lombok.Data;

import java.util.Set;

@Data
public class UserPreferenceDtoReq {
    private NewsStyle preferredStyle;   // CONCISE | FRIENDLY | NEUTRAL
    private Set<String> likedTags;      // 보고 싶은 태그
    private Set<String> blockedTags;    // 보기 싫은 태그
}

