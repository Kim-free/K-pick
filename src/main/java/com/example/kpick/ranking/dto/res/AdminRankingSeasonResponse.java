package com.example.kpick.ranking.dto.res;

import com.example.kpick.ranking.domain.RankingSeason;
import com.example.kpick.ranking.domain.RankingSeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRankingSeasonResponse {
    private Long rankingSeasonId;
    private String seasonName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RankingSeasonStatus seasonStatus;
    private long participantCount;
    private long participationCount;
    private int rewardTopN;
    private String rewardDescription;

    public static AdminRankingSeasonResponse from(RankingSeason season, long participantCount, long participationCount) {
        return new AdminRankingSeasonResponse(
                season.getId(),
                season.getSeasonName(),
                season.getStartDate(),
                season.getEndDate(),
                season.getSeasonStatus(),
                participantCount,
                participationCount,
                season.getRewardTopN(),
                season.getRewardDescription()
        );
    }
}
