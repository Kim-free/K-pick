package com.example.kpick.notification.dto.req;

import com.example.kpick.notification.domain.PushNotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BroadcastPushNotificationRequest {
    private PushNotificationType notificationType;
    private String title;
    private String body;
    private String targetType;
    private Long targetId;
}
