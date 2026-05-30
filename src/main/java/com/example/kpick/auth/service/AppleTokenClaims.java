package com.example.kpick.auth.service;

public class AppleTokenClaims {
    private final String subject;
    private final String email;
    private final Boolean emailVerified;
    private final String issuer;
    private final String audience;
    private final Long expiration;

    public AppleTokenClaims(String subject, String email, Boolean emailVerified, String issuer, String audience, Long expiration) {
        this.subject = subject;
        this.email = email;
        this.emailVerified = emailVerified;
        this.issuer = issuer;
        this.audience = audience;
        this.expiration = expiration;
    }

    public String getSubject() {
        return subject;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public Long getExpiration() {
        return expiration;
    }
}
