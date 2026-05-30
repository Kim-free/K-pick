package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.ProfileBadge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileBadgeResponse {
    private Long profileId;
    private int acquiredCount;
    private List<ProfileBadgeItemResponse> acquiredBadges;
    private List<ProfileBadgeItemResponse> lockedBadges;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileBadgeItemResponse {
        private String badgeCode;
        private String badgeName;
        private String description;
        private String emoji;
        private boolean acquired;

        public static ProfileBadgeItemResponse acquired(ProfileBadge badge) {
            return new ProfileBadgeItemResponse(
                    badge.getBadgeCode(),
                    badge.getBadgeName(),
                    badge.getDescription(),
                    badge.getEmoji(),
                    true
            );
        }
    }
}
