package com.example.kpick.profile.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class ProfileBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String badgeCode;
    private String badgeName;
    private String description;
    private String emoji;

    public static ProfileBadge create(Long profileId, String badgeCode, String badgeName, String description, String emoji) {
        return ProfileBadge.builder()
                .profileId(profileId)
                .badgeCode(badgeCode)
                .badgeName(badgeName)
                .description(description)
                .emoji(emoji)
                .build();
    }
}
