package com.example.kpick.notification.dto.res;

import com.example.kpick.notification.domain.PushNotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadcastPushNotificationResponse {
    private PushNotificationType notificationType;
    private int targetedProfileCount;
}
