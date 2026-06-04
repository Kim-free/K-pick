package com.example.kpick.notification.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePushNotificationSettingRequest {
    private Boolean missionResultEnabled;
    private Boolean pointRewardEnabled;
    private Boolean interestedProgramEnabled;
    private Boolean commentEnabled;
    private Boolean likeEnabled;
    private Boolean rankingTierChangeEnabled;
    private Boolean specialBadgeEnabled;
    private Boolean growthBadgeLevelUpEnabled;
    private Boolean eventNoticeEnabled;
    private Boolean updateNoticeEnabled;
}
