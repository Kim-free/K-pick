package com.example.kpick.notification.domain;

import com.example.kpick.notification.dto.req.UpdatePushNotificationSettingRequest;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushNotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public static PushNotificationSetting defaultSetting(Long profileId) {
        return PushNotificationSetting.builder()
                .profileId(profileId)
                .missionResultEnabled(true)
                .pointRewardEnabled(true)
                .interestedProgramEnabled(false)
                .commentEnabled(true)
                .likeEnabled(false)
                .rankingTierChangeEnabled(true)
                .specialBadgeEnabled(true)
                .growthBadgeLevelUpEnabled(false)
                .attendanceReminderEnabled(true)
                .eventNoticeEnabled(true)
                .updateNoticeEnabled(false)
                .build();
    }

    public void update(UpdatePushNotificationSettingRequest request) {
        missionResultEnabled = valueOrCurrent(request.getMissionResultEnabled(), missionResultEnabled);
        pointRewardEnabled = valueOrCurrent(request.getPointRewardEnabled(), pointRewardEnabled);
        interestedProgramEnabled = valueOrCurrent(request.getInterestedProgramEnabled(), interestedProgramEnabled);
        commentEnabled = valueOrCurrent(request.getCommentEnabled(), commentEnabled);
        likeEnabled = valueOrCurrent(request.getLikeEnabled(), likeEnabled);
        rankingTierChangeEnabled = valueOrCurrent(request.getRankingTierChangeEnabled(), rankingTierChangeEnabled);
        specialBadgeEnabled = valueOrCurrent(request.getSpecialBadgeEnabled(), specialBadgeEnabled);
        growthBadgeLevelUpEnabled = valueOrCurrent(request.getGrowthBadgeLevelUpEnabled(), growthBadgeLevelUpEnabled);
        attendanceReminderEnabled = valueOrCurrent(request.getAttendanceReminderEnabled(), attendanceReminderEnabled);
        eventNoticeEnabled = valueOrCurrent(request.getEventNoticeEnabled(), eventNoticeEnabled);
        updateNoticeEnabled = valueOrCurrent(request.getUpdateNoticeEnabled(), updateNoticeEnabled);
    }

    public boolean isEnabled(PushNotificationType notificationType) {
        return switch (notificationType) {
            case MISSION_RESULT -> missionResultEnabled;
            case MISSION_CLOSING_SOON -> interestedProgramEnabled;
            case POINT_REWARD -> pointRewardEnabled;
            case INTERESTED_PROGRAM -> interestedProgramEnabled;
            case COMMENT -> commentEnabled;
            case LIKE -> likeEnabled;
            case RANKING_TIER_CHANGE -> rankingTierChangeEnabled;
            case SPECIAL_BADGE -> specialBadgeEnabled;
            case GROWTH_BADGE_LEVEL_UP -> growthBadgeLevelUpEnabled;
            case ATTENDANCE_REMINDER -> attendanceReminderEnabled;
            case EVENT_NOTICE -> eventNoticeEnabled;
            case UPDATE_NOTICE -> updateNoticeEnabled;
        };
    }

    private boolean valueOrCurrent(Boolean value, boolean current) {
        return value == null ? current : value;
    }
}
