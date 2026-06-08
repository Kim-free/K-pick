package com.example.kpick.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;

    @Enumerated(EnumType.STRING)
    private PushNotificationType notificationType;

    private String title;
    private String body;
    private String targetType;
    private Long targetId;

    @Enumerated(EnumType.STRING)
    private PushDeliveryStatus deliveryStatus;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static PushNotification create(
            Long profileId,
            PushNotificationType notificationType,
            String title,
            String body,
            String targetType,
            Long targetId,
            PushDeliveryStatus deliveryStatus
    ) {
        return PushNotification.builder()
                .profileId(profileId)
                .notificationType(notificationType)
                .title(title)
                .body(body)
                .targetType(targetType)
                .targetId(targetId)
                .deliveryStatus(deliveryStatus)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markRead() {
        deliveryStatus = PushDeliveryStatus.READ;
        readAt = LocalDateTime.now();
    }

    public void markSent() {
        deliveryStatus = PushDeliveryStatus.SENT;
    }

    public void markFailed() {
        deliveryStatus = PushDeliveryStatus.FAILED;
    }
}
