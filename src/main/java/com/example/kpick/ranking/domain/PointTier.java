package com.example.kpick.ranking.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PointTier {
    ALTORI(1, "알토리", 0, 99, true),
    BABY_TORI(2, "아기토리", 100, 299, true),
    LITTLE_TORI(3, "꼬마토리", 300, 699, true),
    PUBERTY_TORI(4, "사춘기토리", 700, 1499, true),
    NEWBIE_TORI(5, "새내기토리", 1500, 2999, true),
    OFFICE_TORI(6, "직장인토리", 3000, 5499, true),
    VETERAN_TORI(7, "베테랑토리", 5500, 8999, true),
    MASTER_TORI(8, "마스터토리", 9000, 14999, true),
    LEGEND_TORI(9, "전설토리", 15000, Long.MAX_VALUE, false);

    private final int step;
    private final String tierName;
    private final long minPoint;
    private final long maxPoint;
    private final boolean hasSubLevel;

    PointTier(int step, String tierName, long minPoint, long maxPoint, boolean hasSubLevel) {
        this.step = step;
        this.tierName = tierName;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.hasSubLevel = hasSubLevel;
    }

    public static PointTier from(long point) {
        return Arrays.stream(values())
                .filter(tier -> point >= tier.minPoint && point <= tier.maxPoint)
                .findFirst()
                .orElse(LEGEND_TORI);
    }

    public PointTier next() {
        PointTier[] tiers = values();
        int nextIndex = ordinal() + 1;
        if (nextIndex >= tiers.length) {
            return null;
        }
        return tiers[nextIndex];
    }

    public Integer getSubLevel(long point) {
        if (!hasSubLevel) {
            return null;
        }

        long normalizedPoint = Math.max(minPoint, Math.min(point, maxPoint)) - minPoint;
        long pointRange = maxPoint - minPoint + 1;
        long levelSize = (long) Math.ceil(pointRange / 3.0);

        if (normalizedPoint < levelSize) {
            return 3;
        }
        if (normalizedPoint < levelSize * 2) {
            return 2;
        }
        return 1;
    }

    public String getDisplayName(long point) {
        Integer subLevel = getSubLevel(point);
        if (subLevel == null) {
            return tierName;
        }
        return tierName + " " + subLevel;
    }
}
