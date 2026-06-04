package com.example.kpick.notification.controller;

import com.example.kpick.notification.dto.req.BroadcastPushNotificationRequest;
import com.example.kpick.notification.dto.res.BroadcastPushNotificationResponse;
import com.example.kpick.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class PushNotificationAdminController {
    private final PushNotificationService pushNotificationService;

    @PostMapping("/broadcast")
    public ResponseEntity<BroadcastPushNotificationResponse> broadcast(
            @RequestBody BroadcastPushNotificationRequest request
    ) {
        return ResponseEntity.ok(pushNotificationService.broadcast(request));
    }
}
