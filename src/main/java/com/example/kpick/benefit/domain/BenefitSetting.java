package com.example.kpick.benefit.domain;

import com.example.kpick.benefit.dto.req.UpdateBenefitSettingRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class BenefitSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int dailyAttendancePick;
    private int weeklyAttendanceBonusPick;
    private int dailyAdLimit;
    private int adRewardPick;

    public static BenefitSetting defaultSetting() {
        return BenefitSetting.builder()
                .dailyAttendancePick(3)
                .weeklyAttendanceBonusPick(3)
                .dailyAdLimit(10)
                .adRewardPick(1)
                .build();
    }

    public void update(UpdateBenefitSettingRequest request) {
        if (request.getDailyAttendancePick() != null) {
            this.dailyAttendancePick = request.getDailyAttendancePick();
        }
        if (request.getWeeklyAttendanceBonusPick() != null) {
            this.weeklyAttendanceBonusPick = request.getWeeklyAttendanceBonusPick();
        }
        if (request.getDailyAdLimit() != null) {
            this.dailyAdLimit = request.getDailyAdLimit();
        }
        if (request.getAdRewardPick() != null) {
            this.adRewardPick = request.getAdRewardPick();
        }
    }
}
