package com.example.kpick.auth.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApplePublicKeyClient {
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final Duration CACHE_TTL = Duration.ofHours(12);

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private List<ApplePublicKey> cachedKeys = List.of();
    private Instant cachedAt = Instant.EPOCH;

    public ApplePublicKey findByKidAndAlg(String kid, String alg) {
        return getKeys().stream()
                .filter(key -> key.getKid().equals(kid))
                .filter(key -> key.getAlg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Matching Apple public key not found."));
    }

    private List<ApplePublicKey> getKeys() {
        if (!cachedKeys.isEmpty() && cachedAt.plus(CACHE_TTL).isAfter(Instant.now())) {
            return cachedKeys;
        }
        cachedKeys = fetchKeys();
        cachedAt = Instant.now();
        return cachedKeys;
    }

    private List<ApplePublicKey> fetchKeys() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(APPLE_KEYS_URL))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Failed to fetch Apple public keys. status=" + response.statusCode());
            }
            return parseKeys(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch Apple public keys.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fetching Apple public keys was interrupted.", exception);
        }
    }

    private List<ApplePublicKey> parseKeys(String json) {
        List<ApplePublicKey> keys = new ArrayList<>();
        Matcher objectMatcher = Pattern.compile("\\{[^{}]*\"kty\"\\s*:\\s*\"RSA\"[^{}]*}").matcher(json);
        while (objectMatcher.find()) {
            String keyJson = objectMatcher.group();
            keys.add(new ApplePublicKey(
                    extractString(keyJson, "kid"),
                    extractString(keyJson, "alg"),
                    extractString(keyJson, "n"),
                    extractString(keyJson, "e")
            ));
        }
        if (keys.isEmpty()) {
            throw new IllegalStateException("Apple public keys response is empty.");
        }
        return keys;
    }

    private String extractString(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Apple public key field is missing. fieldName=" + fieldName);
        }
        return matcher.group(1);
    }
}
