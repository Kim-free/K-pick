package com.example.kpick.admob.controller;

import com.example.kpick.admob.dto.req.AdMobSsvCallbackRequest;
import com.example.kpick.admob.service.AdMobSsvCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdMobSsvCallbackController {
    private final AdMobSsvCallbackService adMobSsvCallbackService;

    @GetMapping("/v1/ads/admob/callback")
    public ResponseEntity<Void> handleAdMobSsvCallback(@RequestParam Map<String, String> queryParameters) {
        adMobSsvCallbackService.handleCallback(AdMobSsvCallbackRequest.from(queryParameters));
        return ResponseEntity.ok().build();
    }
}
