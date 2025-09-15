package com.ohnew.ohnew.entity;

import com.ohnew.ohnew.converter.UserPreferenceConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = UserPreferenceConverter.class)
    private List<String> favoriteTopics;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Convert(converter = UserPreferenceConverter.class)
    private List<String> lessFavoriteTopics;

    @Column(nullable = false)
    private String preferredStyles; // 보고 싶은 문체
}
