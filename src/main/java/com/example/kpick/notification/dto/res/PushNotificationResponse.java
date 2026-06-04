package com.example.kpick.notification.dto.res;

import com.example.kpick.notification.domain.PushDeliveryStatus;
import com.example.kpick.notification.domain.PushNotification;
import com.example.kpick.notification.domain.PushNotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PushNotificationResponse {
    private Long pushNotificationId;
    private PushNotificationType notificationType;
    private String title;
    private String body;
    private String targetType;
    private Long targetId;
    private PushDeliveryStatus deliveryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static PushNotificationResponse from(PushNotification notification) {
        return new PushNotificationResponse(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getDeliveryStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
