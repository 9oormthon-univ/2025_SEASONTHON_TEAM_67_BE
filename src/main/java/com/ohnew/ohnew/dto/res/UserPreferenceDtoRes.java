package com.ohnew.ohnew.dto.res;

import com.ohnew.ohnew.entity.enums.NewsStyle;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserPreferenceDtoRes {
    private NewsStyle preferredStyle;
    private Set<String> likedTags;
    private Set<String> blockedTags;
}
