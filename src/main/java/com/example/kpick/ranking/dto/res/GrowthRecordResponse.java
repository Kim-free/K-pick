package com.example.kpick.ranking.dto.res;

import com.example.kpick.profile.domain.Profile;
import com.example.kpick.ranking.domain.PointScope;
import com.example.kpick.ranking.domain.PointTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecordResponse {
    private Long profileId;
    private String nickname;
    private PointScope pointScope;
    private long point;
    private PointTierResponse currentTier;
    private String nextTierMessage;
    private List<TierProgressResponse> tiers;

    public static GrowthRecordResponse from(Profile profile, PointScope pointScope, long point) {
        PointTier currentTier = PointTier.from(point);
        PointTierResponse currentTierResponse = PointTierResponse.from(point);

        return new GrowthRecordResponse(
                profile.getId(),
                profile.getNickname(),
                pointScope,
                point,
                currentTierResponse,
                createNextTierMessage(currentTierResponse),
                Arrays.stream(PointTier.values())
                        .map(tier -> TierProgressResponse.from(tier, currentTier, point))
                        .toList()
        );
    }

    private static String createNextTierMessage(PointTierResponse currentTier) {
        if (currentTier.getNextTierName() == null) {
            return "전설토리 달성";
        }
        return currentTier.getNextTierName() + "까지 " + currentTier.getPointsToNextTier() + "pt 남음";
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierProgressResponse {
        private int step;
        private String tierName;
        private long minPoint;
        private Long maxPoint;
        private Integer subLevel;
        private boolean completed;
        private boolean current;

        public static TierProgressResponse from(PointTier tier, PointTier currentTier, long point) {
            boolean current = tier == currentTier;
            return new TierProgressResponse(
                    tier.getStep(),
                    tier.getTierName(),
                    tier.getMinPoint(),
                    tier.getMaxPoint() == Long.MAX_VALUE ? null : tier.getMaxPoint(),
                    current ? tier.getSubLevel(point) : null,
                    point > tier.getMaxPoint(),
                    current
            );
        }
    }
}
