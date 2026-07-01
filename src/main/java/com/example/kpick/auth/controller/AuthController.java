package com.example.kpick.auth.controller;

import com.example.kpick.auth.dto.req.OAuthLoginRequest;
import com.example.kpick.auth.dto.req.TestTokenRequest;
import com.example.kpick.auth.dto.res.AuthResponse;
import com.example.kpick.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Value("${oauth2.apple.android-deep-link-uri:picktory://auth/callback}")
    private String appleAndroidDeepLinkUri;

    @PostMapping("/apple")
    public ResponseEntity<AuthResponse> loginWithApple(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithAppleIos(request));
    }

    @PostMapping("/apple/ios")
    public ResponseEntity<AuthResponse> loginWithAppleIos(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithAppleIos(request));
    }

    @RequestMapping(value = "/apple/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> handleAppleAndroidCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (error != null && !error.isBlank()) {
            return redirectToAppleAndroidDeepLink(
                    UriComponentsBuilder.fromUriString(appleAndroidDeepLinkUri)
                            .queryParam("error", error)
                            .queryParamIfPresent("state", java.util.Optional.ofNullable(blankToNull(state)))
            );
        }
        if (code == null || code.isBlank()) {
            return redirectToAppleAndroidDeepLink(
                    UriComponentsBuilder.fromUriString(appleAndroidDeepLinkUri)
                            .queryParam("error", "missing_code")
                            .queryParamIfPresent("state", java.util.Optional.ofNullable(blankToNull(state)))
            );
        }

        try {
            AuthResponse authResponse = authService.loginWithAppleAndroidCallback(code);
            return redirectToAppleAndroidDeepLink(
                    UriComponentsBuilder.fromUriString(appleAndroidDeepLinkUri)
                            .queryParam("token", authResponse.getAccessToken())
                            .queryParam("tokenType", authResponse.getTokenType())
                            .queryParam("newUser", authResponse.isNewUser())
                            .queryParam("signUpStatus", authResponse.getSignUpStatus())
                            .queryParamIfPresent("state", java.util.Optional.ofNullable(blankToNull(state)))
            );
        } catch (RuntimeException exception) {
            log.warn("Apple Android callback login failed.", exception);
            return redirectToAppleAndroidDeepLink(
                    UriComponentsBuilder.fromUriString(appleAndroidDeepLinkUri)
                            .queryParam("error", "oauth_failed")
                            .queryParamIfPresent("state", java.util.Optional.ofNullable(blankToNull(state)))
            );
        }
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> loginWithKakao(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithKakao(request));
    }

    @PostMapping("/test-token")
    public ResponseEntity<AuthResponse> createTestToken(@RequestBody TestTokenRequest request) {
        return ResponseEntity.ok(authService.createTestToken(request));
    }

    private ResponseEntity<Void> redirectToAppleAndroidDeepLink(UriComponentsBuilder uriBuilder) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(uriBuilder.build().toUriString()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
