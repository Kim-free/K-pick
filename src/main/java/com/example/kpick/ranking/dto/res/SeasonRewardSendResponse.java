package com.example.kpick.ranking.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRewardSendResponse {
    private Long rankingSeasonId;
    private int sentCount;
}
