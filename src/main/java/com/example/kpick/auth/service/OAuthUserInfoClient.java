package com.example.kpick.auth.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OAuthUserInfoClient {
    private static final String GOOGLE_USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OAuthProviderProfile getGoogleProfile(String accessToken) {
        String responseBody = requestUserInfo(GOOGLE_USER_INFO_URL, accessToken);
        return new OAuthProviderProfile(
                extractRequiredString(responseBody, "sub"),
                extractString(responseBody, "email"),
                extractBoolean(responseBody, "email_verified")
        );
    }

    public OAuthProviderProfile getKakaoProfile(String accessToken) {
        String responseBody = requestUserInfo(KAKAO_USER_INFO_URL, accessToken);
        return new OAuthProviderProfile(
                extractRequiredNumber(responseBody, "id"),
                extractString(responseBody, "email"),
                extractBoolean(responseBody, "is_email_verified")
        );
    }

    private String requestUserInfo(String url, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is required.");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + accessToken.trim())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("OAuth accessToken is invalid.");
            }
            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to request OAuth user information.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OAuth user information request was interrupted.", exception);
        }
    }

    private String extractRequiredString(String json, String fieldName) {
        String value = extractString(json, fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OAuth user information is missing. fieldName=" + fieldName);
        }
        return value;
    }

    private String extractString(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractRequiredNumber(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("OAuth user information is missing. fieldName=" + fieldName);
        }
        return matcher.group(1);
    }

    private Boolean extractBoolean(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : null;
    }
}
