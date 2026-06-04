package com.example.kpick.auth.service;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.domain.AppUserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtProvider {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public String createAccessToken(AppUser appUser, Long profileId) {
        long now = Instant.now().toEpochMilli();
        long expiration = now + accessTokenExpiration;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{"
                + "\"sub\":\"" + appUser.getId() + "\","
                + "\"appUserId\":" + appUser.getId() + ","
                + "\"profileId\":" + profileId + ","
                + "\"loginType\":\"" + appUser.getLoginType() + "\","
                + "\"role\":\"" + appUser.getRoleOrDefault() + "\","
                + "\"iat\":" + (now / 1000) + ","
                + "\"exp\":" + (expiration / 1000)
                + "}";
        String encodedHeader = base64Url(header);
        String encodedPayload = base64Url(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims parseAndValidate(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        String[] tokenParts = accessToken.split("\\.");
        if (tokenParts.length != 3) {
            throw new IllegalArgumentException("Invalid accessToken.");
        }
        String unsignedToken = tokenParts[0] + "." + tokenParts[1];
        if (!MessageDigest.isEqual(
                sign(unsignedToken).getBytes(StandardCharsets.UTF_8),
                tokenParts[2].getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IllegalArgumentException("Invalid accessToken signature.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
        Long expiration = extractLong(payload, "exp");
        if (expiration == null || expiration < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("accessToken is expired.");
        }
        Long appUserId = extractLong(payload, "appUserId");
        Long profileId = extractLong(payload, "profileId");
        AppUserRole appUserRole = extractRole(payload);
        if (appUserId == null || profileId == null) {
            throw new IllegalArgumentException("Invalid accessToken payload.");
        }
        return new JwtClaims(appUserId, profileId, appUserRole);
    }

    private String base64Url(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create JWT.", exception);
        }
    }

    private Long extractLong(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private AppUserRole extractRole(String json) {
        Matcher matcher = Pattern.compile("\"role\"\\s*:\\s*\"([A-Z_]+)\"").matcher(json);
        if (!matcher.find()) {
            return AppUserRole.USER;
        }
        try {
            return AppUserRole.valueOf(matcher.group(1));
        } catch (IllegalArgumentException exception) {
            return AppUserRole.USER;
        }
    }
}
