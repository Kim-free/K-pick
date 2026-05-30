package com.example.kpick.auth.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleLoginRequest {
    private String identityToken;
    private String authorizationCode;
    private String fullName;
}
