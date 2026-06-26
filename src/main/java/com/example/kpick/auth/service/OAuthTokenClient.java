package com.example.kpick.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OAuthTokenClient {
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AppleClientSecretProvider appleClientSecretProvider;

    @Value("${oauth2.apple.client-id}")
    private String appleClientId;

    @Value("${oauth2.apple.android-redirect-uri:${oauth2.apple.redirect-uri:}}")
    private String appleAndroidRedirectUri;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    public OAuthTokenClient(AppleClientSecretProvider appleClientSecretProvider) {
        this.appleClientSecretProvider = appleClientSecretProvider;
    }

    public OAuthTokenResponse exchangeAppleIosCode(String authorizationCode) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", appleClientId);
        parameters.put("client_secret", appleClientSecretProvider.createClientSecret());
        parameters.put("code", authorizationCode);
        parameters.put("grant_type", "authorization_code");
        return requestToken(APPLE_TOKEN_URL, parameters);
    }

    public OAuthTokenResponse exchangeAppleAndroidCode(String authorizationCode) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", appleClientId);
        parameters.put("client_secret", appleClientSecretProvider.createClientSecret());
        parameters.put("code", authorizationCode);
        parameters.put("grant_type", "authorization_code");
        putIfNotBlank(parameters, "redirect_uri", appleAndroidRedirectUri);
        return requestToken(APPLE_TOKEN_URL, parameters);
    }

    public OAuthTokenResponse exchangeGoogleCode(String authorizationCode) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("client_id", googleClientId);
        putIfNotBlank(parameters, "client_secret", googleClientSecret);
        parameters.put("code", authorizationCode);
        parameters.put("grant_type", "authorization_code");
        putIfNotBlank(parameters, "redirect_uri", googleRedirectUri);
        return requestToken(GOOGLE_TOKEN_URL, parameters);
    }

    public OAuthTokenResponse exchangeKakaoCode(String authorizationCode) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("grant_type", "authorization_code");
        parameters.put("client_id", kakaoClientId);
        parameters.put("redirect_uri", kakaoRedirectUri);
        parameters.put("code", authorizationCode);
        putIfNotBlank(parameters, "client_secret", kakaoClientSecret);
        return requestToken(KAKAO_TOKEN_URL, parameters);
    }

    private OAuthTokenResponse requestToken(String url, Map<String, String> parameters) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormData(parameters)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("OAuth authorizationCode is invalid.");
            }
            JsonNode json = objectMapper.readTree(response.body());
            return new OAuthTokenResponse(
                    textValue(json, "access_token"),
                    textValue(json, "id_token"),
                    textValue(json, "refresh_token")
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to exchange OAuth authorizationCode.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OAuth token request was interrupted.", exception);
        }
    }

    private String toFormData(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void putIfNotBlank(Map<String, String> parameters, String key, String value) {
        if (value != null && !value.isBlank()) {
            parameters.put(key, value);
        }
    }

    private String textValue(JsonNode json, String fieldName) {
        JsonNode value = json.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }
}
