package com.example.kpick.benefit.dto.res;

import com.example.kpick.benefit.domain.BenefitSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BenefitSettingResponse {
    private Long benefitSettingId;
    private int dailyAttendancePick;
    private int weeklyAttendanceBonusPick;
    private int dailyAdLimit;
    private int adRewardPick;

    public static BenefitSettingResponse from(BenefitSetting setting) {
        return new BenefitSettingResponse(
                setting.getId(),
                setting.getDailyAttendancePick(),
                setting.getWeeklyAttendanceBonusPick(),
                setting.getDailyAdLimit(),
                setting.getAdRewardPick()
        );
    }
}
