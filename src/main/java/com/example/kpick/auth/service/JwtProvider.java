package com.example.kpick.auth.service;

import com.example.kpick.appUser.domain.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

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
                + "\"iat\":" + (now / 1000) + ","
                + "\"exp\":" + (expiration / 1000)
                + "}";
        String encodedHeader = base64Url(header);
        String encodedPayload = base64Url(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return unsignedToken + "." + sign(unsignedToken);
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
}
