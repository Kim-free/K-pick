package com.example.kpick.benefit.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BenefitHomeResponse {
    private Long profileId;
    private AttendanceBenefitResponse attendance;
    private AdBenefitResponse ad;
    private List<MiniGameResponse> miniGames;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceBenefitResponse {
        private int consecutiveAttendanceDays;
        private int dailyAttendancePick;
        private int weeklyAttendanceBonusPick;
        private boolean checkedToday;
        private List<AttendanceDayResponse> week;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceDayResponse {
        private LocalDate attendanceDate;
        private String dayOfWeek;
        private int rewardPick;
        private boolean checked;
        private boolean today;
        private boolean weeklyBonusDay;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdBenefitResponse {
        private int dailyAdLimit;
        private int watchedCount;
        private int remainingCount;
        private int rewardPick;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiniGameResponse {
        private String gameCode;
        private String gameName;
        private String status;
    }
}
