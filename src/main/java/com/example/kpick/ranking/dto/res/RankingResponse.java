package com.example.kpick.ranking.dto.res;

import com.example.kpick.ranking.domain.RankingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private RankingType rankingType;
    private String seasonName;
    private LocalDate seasonStartDate;
    private LocalDate seasonEndDate;
    private Long daysUntilSeasonEnd;
    private int totalUserCount;
    private List<RankingEntryResponse> topThree;
    private List<RankingEntryResponse> rankings;
    private MyRankingCardResponse myRanking;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyRankingCardResponse {
        private int rank;
        private Long profileId;
        private String nickname;
        private long score;
        private PointTierResponse pointTier;
        private String nextStepMessage;
    }
}
