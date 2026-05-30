package com.example.kpick.benefit.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateBenefitSettingRequest {
    private Integer dailyAttendancePick;
    private Integer weeklyAttendanceBonusPick;
    private Integer dailyAdLimit;
    private Integer adRewardPick;
}
