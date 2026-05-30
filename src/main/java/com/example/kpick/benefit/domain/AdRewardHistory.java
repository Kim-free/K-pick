package com.example.kpick.benefit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class AdRewardHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private LocalDate rewardDate;
    private LocalDateTime createdAt;

    public static AdRewardHistory create(Long profileId, LocalDate rewardDate) {
        return AdRewardHistory.builder()
                .profileId(profileId)
                .rewardDate(rewardDate)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
