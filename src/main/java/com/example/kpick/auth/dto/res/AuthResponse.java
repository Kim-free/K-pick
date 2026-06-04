package com.example.kpick.auth.dto.res;

import com.example.kpick.profile.domain.SignUpStatus;
import com.example.kpick.appUser.domain.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long appUserId;
    private Long profileId;
    private String accessToken;
    private String tokenType;
    private boolean newUser;
    private SignUpStatus signUpStatus;
    private AppUserRole appUserRole;
}
