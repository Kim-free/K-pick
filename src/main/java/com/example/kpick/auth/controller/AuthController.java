package com.example.kpick.auth.controller;

import com.example.kpick.auth.dto.req.OAuthLoginRequest;
import com.example.kpick.auth.dto.req.TestTokenRequest;
import com.example.kpick.auth.dto.res.AuthResponse;
import com.example.kpick.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/apple")
    public ResponseEntity<AuthResponse> loginWithApple(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithApple(request));
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
}
