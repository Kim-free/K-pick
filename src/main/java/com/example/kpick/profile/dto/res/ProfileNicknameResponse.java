package com.example.kpick.profile.dto.res;

import com.example.kpick.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileNicknameResponse {
    private Long profileId;
    private String nickname;

    public static ProfileNicknameResponse from(Profile profile) {
        return new ProfileNicknameResponse(profile.getId(), profile.getNickname());
    }
}
