package com.example.kpick.profile.dto.req;

import com.example.kpick.profile.domain.ProfileGender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOnboardingProfileRequest {
    private String nickname;
    private String profileImageUrl;
    private ProfileGender gender;
    private LocalDate birthDate;
    private String joinPath;
    private String friendInviteCode;
}
