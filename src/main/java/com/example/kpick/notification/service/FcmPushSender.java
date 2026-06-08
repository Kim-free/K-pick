package com.example.kpick.notification.service;

import com.example.kpick.notification.domain.PushDeviceToken;
import com.example.kpick.notification.domain.PushNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FcmPushSender {
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public FcmSendResult send(PushNotification notification, List<PushDeviceToken> deviceTokens) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null || deviceTokens == null || deviceTokens.isEmpty()) {
            return FcmSendResult.skipped();
        }

        List<String> tokens = deviceTokens.stream()
                .map(PushDeviceToken::getDeviceToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();
        if (tokens.isEmpty()) {
            return FcmSendResult.skipped();
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .putAllData(createData(notification))
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            return new FcmSendResult(response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException exception) {
            return new FcmSendResult(0, tokens.size());
        }
    }

    private Map<String, String> createData(PushNotification notification) {
        Map<String, String> data = new HashMap<>();
        data.put("pushNotificationId", String.valueOf(notification.getId()));
        data.put("notificationType", notification.getNotificationType().name());
        if (notification.getTargetType() != null) {
            data.put("targetType", notification.getTargetType());
        }
        if (notification.getTargetId() != null) {
            data.put("targetId", String.valueOf(notification.getTargetId()));
        }
        return data;
    }
}
