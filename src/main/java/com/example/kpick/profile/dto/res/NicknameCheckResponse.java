package com.example.kpick.profile.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NicknameCheckResponse {
    private String nickname;
    private boolean available;
}
