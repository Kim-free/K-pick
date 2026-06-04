package com.example.kpick.auth.service;

public class OAuthProviderProfile {
    private final String providerId;
    private final String email;
    private final Boolean emailVerified;

    public OAuthProviderProfile(String providerId, String email, Boolean emailVerified) {
        this.providerId = providerId;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }
}
