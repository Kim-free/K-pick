package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.domain.ProfileGender;
import com.example.kpick.profile.domain.SignUpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingProfileResponse {
    private Long profileId;
    private String nickname;
    private String profileImageUrl;
    private ProfileGender gender;
    private LocalDate birthDate;
    private String joinPath;
    private Long invitedByProfileId;
    private int inviteRewardPick;
    private SignUpStatus signUpStatus;

    public static OnboardingProfileResponse from(Profile profile, int inviteRewardPick) {
        return new OnboardingProfileResponse(
                profile.getId(),
                profile.getNickname(),
                profile.getProfileImageUrl(),
                profile.getProfileGender(),
                profile.getBirthDate(),
                profile.getJoinPath(),
                profile.getInvitedByProfileId(),
                inviteRewardPick,
                profile.getSignUpStatus()
        );
    }
}
