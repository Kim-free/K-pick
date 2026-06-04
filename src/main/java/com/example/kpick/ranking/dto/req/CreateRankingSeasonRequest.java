package com.example.kpick.ranking.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateRankingSeasonRequest {
    private String seasonName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rewardTopN;
    private String rewardDescription;
}
