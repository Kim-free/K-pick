package com.example.kpick.profile.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateProfileBadgeRequest {
    private String badgeCode;
    private String badgeName;
    private String description;
    private String emoji;
}
