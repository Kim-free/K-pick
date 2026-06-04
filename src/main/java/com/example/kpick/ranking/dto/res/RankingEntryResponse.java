package com.example.kpick.ranking.dto.res;

import com.example.kpick.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntryResponse {
    private int rank;
    private Long profileId;
    private String nickname;
    private String profileImageUrl;
    private long score;
    private PointTierResponse pointTier;
    private Long threadCount;
    private Long commentCount;

    public static RankingEntryResponse from(int rank, Profile profile, long score) {
        return new RankingEntryResponse(
                rank,
                profile.getId(),
                profile.getNickname(),
                profile.getProfileImageUrl(),
                score,
                PointTierResponse.from(score),
                null,
                null
        );
    }

    public static RankingEntryResponse communityFrom(int rank, Profile profile, long score, long threadCount, long commentCount) {
        return new RankingEntryResponse(
                rank,
                profile.getId(),
                profile.getNickname(),
                profile.getProfileImageUrl(),
                score,
                null,
                threadCount,
                commentCount
        );
    }
}
