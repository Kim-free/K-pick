package com.example.kpick.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AppleIdentityTokenParser {
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    @Value("${oauth2.apple.client-id}")
    private String clientId;

    public AppleIdentityTokenParser(AppleIdentityTokenVerifier appleIdentityTokenVerifier) {
        this.appleIdentityTokenVerifier = appleIdentityTokenVerifier;
    }

    public AppleTokenClaims parseAndValidate(String identityToken) {
        if (identityToken == null || identityToken.isBlank()) {
            throw new IllegalArgumentException("identityToken is required.");
        }
        String[] tokenParts = identityToken.split("\\.");
        if (tokenParts.length < 2) {
            throw new IllegalArgumentException("Invalid Apple identityToken.");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]), StandardCharsets.UTF_8);
        String payloadJson = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
        String kid = extractString(headerJson, "kid");
        String alg = extractString(headerJson, "alg");
        appleIdentityTokenVerifier.verify(identityToken, kid, alg);

        AppleTokenClaims claims = new AppleTokenClaims(
                extractString(payloadJson, "sub"),
                extractString(payloadJson, "email"),
                extractBoolean(payloadJson, "email_verified"),
                extractString(payloadJson, "iss"),
                extractString(payloadJson, "aud"),
                extractLong(payloadJson, "exp")
        );
        validateClaims(claims);
        return claims;
    }

    private void validateClaims(AppleTokenClaims claims) {
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new IllegalArgumentException("Apple providerId is missing.");
        }
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new IllegalArgumentException("Invalid Apple token issuer.");
        }
        if (!clientId.equals(claims.getAudience())) {
            throw new IllegalArgumentException("Invalid Apple token audience.");
        }
        if (claims.getExpiration() == null || claims.getExpiration() < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("Apple identityToken is expired.");
        }
    }

    private String extractString(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Boolean extractBoolean(String json, String fieldName) {
        String stringValue = extractString(json, fieldName);
        if (stringValue != null) {
            return Boolean.parseBoolean(stringValue);
        }
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : null;
    }

    private Long extractLong(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }
}
