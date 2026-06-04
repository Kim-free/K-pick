package com.example.kpick.ranking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rankingSeasonId;
    private Long profileId;
    private int rank;
    private long seasonPoint;
    private String rewardDescription;
    private Boolean recipientInfoSubmitted;

    @Enumerated(EnumType.STRING)
    private SeasonRewardStatus rewardStatus;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public void markSent() {
        this.rewardStatus = SeasonRewardStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
}
