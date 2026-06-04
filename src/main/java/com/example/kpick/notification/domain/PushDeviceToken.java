package com.example.kpick.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushDeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String deviceToken;
    private String platform;
    private LocalDateTime updatedAt;

    public static PushDeviceToken create(Long profileId, String deviceToken, String platform) {
        return PushDeviceToken.builder()
                .profileId(profileId)
                .deviceToken(deviceToken)
                .platform(platform)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void update(Long profileId, String platform) {
        this.profileId = profileId;
        this.platform = platform;
        this.updatedAt = LocalDateTime.now();
    }
}
