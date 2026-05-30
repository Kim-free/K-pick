package com.example.kpick.ranking.dto.res;

import com.example.kpick.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyRankingResponse {
    private Long profileId;
    private String nickname;
    private PointTierResponse pointTier;
    private int seasonRank;
    private int totalRank;
    private long seasonPoint;
    private long totalMissionPoint;
    private int correctRate;
    private List<String> specialBadges;

    public static MyRankingResponse from(
            Profile profile,
            int seasonRank,
            int totalRank,
            int correctRate,
            List<String> specialBadges
    ) {
        return new MyRankingResponse(
                profile.getId(),
                profile.getNickname(),
                PointTierResponse.from(profile.getMissionPointValue()),
                seasonRank,
                totalRank,
                profile.getMissionPointValue(),
                profile.getTotalMissionPointValue(),
                correctRate,
                specialBadges
        );
    }
}
