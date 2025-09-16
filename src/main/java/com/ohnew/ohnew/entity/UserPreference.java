package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.entity.enums.NewsStyle;
import com.ohnew.ohnew.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_preference")
public class UserPreference extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NewsStyle preferredStyle = NewsStyle.NEUTRAL;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preference_liked_tags", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "tag")
    private java.util.Set<String> likedTags = new java.util.HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preference_blocked_tags", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "tag")
    private java.util.Set<String> blockedTags = new java.util.HashSet<>();

    @Builder
    private UserPreference(User user, NewsStyle preferredStyle, Set<String> likedTags, Set<String> blockedTags) {
        this.user = java.util.Objects.requireNonNull(user);
        if (preferredStyle != null) this.preferredStyle = preferredStyle;
        if (likedTags != null) this.likedTags.addAll(likedTags);
        if (blockedTags != null) this.blockedTags.addAll(blockedTags);
    }

    // 도메인 메서드
    public void changeStyle(NewsStyle style) { if (style != null) this.preferredStyle = style; }

    public void setLikedTags(Set<String> tags) {
        this.likedTags.clear();
        if (tags != null) this.likedTags.addAll(normalize(tags)); // 정규화
    }
    public void setBlockedTags(Set<String> tags) {
        this.blockedTags.clear();
        if (tags != null) this.blockedTags.addAll(normalize(tags)); // 정규화
    }
    public boolean addLikedTag(String tag) {
        String t = tag == null ? null : tag.trim();
        return (t != null && !t.isEmpty()) && this.likedTags.add(t);
    }
    public boolean removeLikedTag(String tag) { return this.likedTags.remove(tag); }
    public boolean addBlockedTag(String tag) {
        String t = tag == null ? null : tag.trim();
        return (t != null && !t.isEmpty()) && this.blockedTags.add(t);
    }
    public boolean removeBlockedTag(String tag) { return this.blockedTags.remove(tag); }

    // 정규화 유틸
    private static java.util.Set<String> normalize(java.util.Set<String> src) {
        return src.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
