package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.Profile;
import com.example.kpick.ranking.dto.res.PointTierResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    private Long profileId;
    private String nickname;
    private String profileImageUrl;
    private PointTierResponse pointTier;
    private int seasonRank;
    private long seasonPoint;
    private int totalRank;
    private long totalPoint;
    private int communityRank;
    private long communityPoint;
    private long coin;
    private String growthMessage;
    private String inviteCode;

    public static MyPageResponse from(
            Profile profile,
            int seasonRank,
            int totalRank,
            int communityRank
    ) {
        PointTierResponse pointTier = PointTierResponse.from(profile.getTotalMissionPointValue());
        return new MyPageResponse(
                profile.getId(),
                profile.getNickname(),
                profile.getProfileImageUrl(),
                pointTier,
                seasonRank,
                profile.getMissionPointValue(),
                totalRank,
                profile.getTotalMissionPointValue(),
                communityRank,
                profile.getActivityPointValue(),
                profile.getCoin() == null ? 0L : profile.getCoin(),
                createGrowthMessage(pointTier),
                profile.getInviteCode()
        );
    }

    private static String createGrowthMessage(PointTierResponse pointTier) {
        if (pointTier.getNextTierName() == null) {
            return "전설토리 달성";
        }
        return pointTier.getNextTierName() + "까지 " + pointTier.getPointsToNextTier() + "pt 남음";
    }

}
