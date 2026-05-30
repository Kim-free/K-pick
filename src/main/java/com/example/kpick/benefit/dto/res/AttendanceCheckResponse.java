package com.example.kpick.benefit.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckResponse {
    private Long profileId;
    private LocalDate attendanceDate;
    private long earnedPick;
    private int consecutiveAttendanceDays;
    private boolean weeklyBonusEarned;
    private long currentCoin;
    private List<BenefitHomeResponse.AttendanceDayResponse> week;
}
