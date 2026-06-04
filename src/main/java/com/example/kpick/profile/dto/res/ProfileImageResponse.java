package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileImageResponse {
    private Long profileId;
    private String profileImageUrl;

    public static ProfileImageResponse from(Profile profile) {
        return new ProfileImageResponse(profile.getId(), profile.getProfileImageUrl());
    }
}
