package com.example.kpick.auth.controller;

import com.example.kpick.auth.dto.req.AppleLoginRequest;
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
    public ResponseEntity<AuthResponse> loginWithApple(@RequestBody AppleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithApple(request));
    }
}
