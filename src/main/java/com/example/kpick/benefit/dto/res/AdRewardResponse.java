package com.example.kpick.benefit.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdRewardResponse {
    private Long profileId;
    private LocalDate rewardDate;
    private long earnedPick;
    private int watchedCount;
    private int remainingCount;
    private long currentCoin;
}
