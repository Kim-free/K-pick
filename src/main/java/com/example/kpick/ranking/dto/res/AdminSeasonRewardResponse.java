package com.example.kpick.ranking.dto.res;

import com.example.kpick.ranking.domain.SeasonRewardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSeasonRewardResponse {
    private Long seasonRewardId;
    private Long profileId;
    private int rank;
    private String nickname;
    private long seasonPoint;
    private String rewardDescription;
    private Boolean recipientInfoSubmitted;
    private SeasonRewardStatus rewardStatus;
}
