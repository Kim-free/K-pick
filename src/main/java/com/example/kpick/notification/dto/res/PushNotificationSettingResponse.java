package com.example.kpick.notification.dto.res;

import com.example.kpick.notification.domain.PushNotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushNotificationSettingResponse {
    private Long profileId;
    private boolean missionResultEnabled;
    private boolean pointRewardEnabled;
    private boolean interestedProgramEnabled;
    private boolean commentEnabled;
    private boolean likeEnabled;
    private boolean rankingTierChangeEnabled;
    private boolean specialBadgeEnabled;
    private boolean growthBadgeLevelUpEnabled;
    private boolean attendanceReminderEnabled;
    private boolean eventNoticeEnabled;
    private boolean updateNoticeEnabled;

    public static PushNotificationSettingResponse from(PushNotificationSetting setting) {
        return new PushNotificationSettingResponse(
                setting.getProfileId(),
                setting.isMissionResultEnabled(),
                setting.isPointRewardEnabled(),
                setting.isInterestedProgramEnabled(),
                setting.isCommentEnabled(),
                setting.isLikeEnabled(),
                setting.isRankingTierChangeEnabled(),
                setting.isSpecialBadgeEnabled(),
                setting.isGrowthBadgeLevelUpEnabled(),
                setting.isAttendanceReminderEnabled(),
                setting.isEventNoticeEnabled(),
                setting.isUpdateNoticeEnabled()
        );
    }
}
