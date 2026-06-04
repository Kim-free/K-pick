package com.example.kpick.auth.service;

import com.example.kpick.appUser.domain.AppUserRole;

public class JwtClaims {
    private final Long appUserId;
    private final Long profileId;
    private final AppUserRole appUserRole;

    public JwtClaims(Long appUserId, Long profileId, AppUserRole appUserRole) {
        this.appUserId = appUserId;
        this.profileId = profileId;
        this.appUserRole = appUserRole;
    }

    public Long getAppUserId() {
        return appUserId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public AppUserRole getAppUserRole() {
        return appUserRole;
    }
}
