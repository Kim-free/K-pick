package com.example.kpick.notification.controller;

import com.example.kpick.notification.dto.req.RegisterPushDeviceTokenRequest;
import com.example.kpick.notification.dto.req.UpdatePushNotificationSettingRequest;
import com.example.kpick.notification.dto.res.PushDeviceTokenResponse;
import com.example.kpick.notification.dto.res.PushNotificationResponse;
import com.example.kpick.notification.dto.res.PushNotificationSettingResponse;
import com.example.kpick.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/me")
public class PushNotificationController {
    private final PushNotificationService pushNotificationService;

    @GetMapping("/settings")
    public ResponseEntity<PushNotificationSettingResponse> getSetting(
            @RequestAttribute("authenticatedProfileId") Long profileId
    ) {
        return ResponseEntity.ok(pushNotificationService.getSetting(profileId));
    }

    @PatchMapping("/settings")
    public ResponseEntity<PushNotificationSettingResponse> updateSetting(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody UpdatePushNotificationSettingRequest request
    ) {
        return ResponseEntity.ok(pushNotificationService.updateSetting(profileId, request));
    }

    @PostMapping("/device-tokens")
    public ResponseEntity<PushDeviceTokenResponse> registerDeviceToken(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody RegisterPushDeviceTokenRequest request
    ) {
        return ResponseEntity.ok(pushNotificationService.registerDeviceToken(profileId, request));
    }

    @GetMapping
    public ResponseEntity<List<PushNotificationResponse>> getNotifications(
            @RequestAttribute("authenticatedProfileId") Long profileId
    ) {
        return ResponseEntity.ok(pushNotificationService.getNotifications(profileId));
    }

    @PatchMapping("/{pushNotificationId}/read")
    public ResponseEntity<PushNotificationResponse> markRead(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @PathVariable Long pushNotificationId
    ) {
        return ResponseEntity.ok(pushNotificationService.markRead(profileId, pushNotificationId));
    }
}
