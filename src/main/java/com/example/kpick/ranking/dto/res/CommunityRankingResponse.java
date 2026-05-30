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
public class CommunityRankingResponse {
    private RankingType rankingType;
    private String periodName;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Long daysUntilPeriodEnd;
    private int totalUserCount;
    private List<RankingEntryResponse> topThree;
    private List<RankingEntryResponse> rankings;
    private MyCommunityRankingCardResponse myRanking;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCommunityRankingCardResponse {
        private int rank;
        private Long profileId;
        private String nickname;
        private long activityPoint;
        private long threadCount;
        private long commentCount;
        private String activityHistoryMessage;
    }
}
