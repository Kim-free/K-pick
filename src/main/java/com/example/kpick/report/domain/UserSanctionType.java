package com.example.kpick.report.domain;

import java.time.LocalDateTime;

public enum UserSanctionType {
    WARNING,
    SUSPEND_7_DAYS,
    SUSPEND_30_DAYS,
    PERMANENT;

    public LocalDateTime calculateEndsAt(LocalDateTime startsAt) {
        return switch (this) {
            case WARNING -> startsAt;
            case SUSPEND_7_DAYS -> startsAt.plusDays(7);
            case SUSPEND_30_DAYS -> startsAt.plusDays(30);
            case PERMANENT -> null;
        };
    }
}
