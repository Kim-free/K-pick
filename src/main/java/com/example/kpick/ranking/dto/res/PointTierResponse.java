package com.example.kpick.ranking.dto.res;

import com.example.kpick.ranking.domain.PointTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointTierResponse {
    private int step;
    private String tierName;
    private Integer subLevel;
    private String displayName;
    private long minPoint;
    private Long maxPoint;
    private String nextTierName;
    private long pointsToNextTier;

    public static PointTierResponse from(long point) {
        PointTier tier = PointTier.from(point);
        PointTier nextTier = tier.next();
        Long maxPoint = tier.getMaxPoint() == Long.MAX_VALUE ? null : tier.getMaxPoint();
        long pointsToNextTier = nextTier == null ? 0 : Math.max(nextTier.getMinPoint() - point, 0);

        return new PointTierResponse(
                tier.getStep(),
                tier.getTierName(),
                tier.getSubLevel(point),
                tier.getDisplayName(point),
                tier.getMinPoint(),
                maxPoint,
                nextTier == null ? null : nextTier.getTierName(),
                pointsToNextTier
        );
    }
}
