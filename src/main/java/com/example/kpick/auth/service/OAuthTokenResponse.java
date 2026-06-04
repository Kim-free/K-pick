package com.example.kpick.auth.service;

public class OAuthTokenResponse {
    private final String accessToken;
    private final String identityToken;
    private final String refreshToken;

    public OAuthTokenResponse(String accessToken, String identityToken, String refreshToken) {
        this.accessToken = accessToken;
        this.identityToken = identityToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
