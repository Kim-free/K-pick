package com.example.kpick.notification.service;

import com.example.kpick.notification.domain.PushDeliveryStatus;
import com.example.kpick.notification.domain.PushDeviceToken;
import com.example.kpick.notification.domain.PushNotification;
import com.example.kpick.notification.domain.PushNotificationSetting;
import com.example.kpick.notification.domain.PushNotificationType;
import com.example.kpick.notification.dto.req.RegisterPushDeviceTokenRequest;
import com.example.kpick.notification.dto.req.BroadcastPushNotificationRequest;
import com.example.kpick.notification.dto.req.UpdatePushNotificationSettingRequest;
import com.example.kpick.notification.dto.res.PushDeviceTokenResponse;
import com.example.kpick.notification.dto.res.BroadcastPushNotificationResponse;
import com.example.kpick.notification.dto.res.PushNotificationResponse;
import com.example.kpick.notification.dto.res.PushNotificationSettingResponse;
import com.example.kpick.notification.repository.PushDeviceTokenRepository;
import com.example.kpick.notification.repository.PushNotificationRepository;
import com.example.kpick.notification.repository.PushNotificationSettingRepository;
import com.example.kpick.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private final PushNotificationSettingRepository pushNotificationSettingRepository;
    private final PushDeviceTokenRepository pushDeviceTokenRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final ProfileRepository profileRepository;
    private final FcmPushSender fcmPushSender;

    @Transactional
    public PushNotificationSettingResponse getSetting(Long profileId) {
        validateProfile(profileId);
        return PushNotificationSettingResponse.from(getOrCreateSetting(profileId));
    }

    @Transactional
    public PushNotificationSettingResponse updateSetting(Long profileId, UpdatePushNotificationSettingRequest request) {
        validateProfile(profileId);
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        PushNotificationSetting setting = getOrCreateSetting(profileId);
        setting.update(request);
        return PushNotificationSettingResponse.from(setting);
    }

    @Transactional
    public PushDeviceTokenResponse registerDeviceToken(Long profileId, RegisterPushDeviceTokenRequest request) {
        validateProfile(profileId);
        if (request == null || request.getDeviceToken() == null || request.getDeviceToken().isBlank()) {
            throw new IllegalArgumentException("deviceToken is required.");
        }
        String platform = request.getPlatform() == null || request.getPlatform().isBlank()
                ? "UNKNOWN"
                : request.getPlatform().trim().toUpperCase();
        PushDeviceToken deviceToken = pushDeviceTokenRepository.findByDeviceToken(request.getDeviceToken().trim())
                .map(existing -> {
                    existing.update(profileId, platform);
                    return existing;
                })
                .orElseGet(() -> pushDeviceTokenRepository.save(
                        PushDeviceToken.create(profileId, request.getDeviceToken().trim(), platform)
                ));
        return PushDeviceTokenResponse.from(deviceToken);
    }

    @Transactional(readOnly = true)
    public List<PushNotificationResponse> getNotifications(Long profileId) {
        validateProfile(profileId);
        return pushNotificationRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                .map(PushNotificationResponse::from)
                .toList();
    }

    @Transactional
    public PushNotificationResponse markRead(Long profileId, Long pushNotificationId) {
        PushNotification notification = pushNotificationRepository.findById(pushNotificationId)
                .orElseThrow(() -> new IllegalArgumentException("Push notification not found. pushNotificationId=" + pushNotificationId));
        if (!notification.getProfileId().equals(profileId)) {
            throw new IllegalArgumentException("Push notification does not belong to this profile.");
        }
        notification.markRead();
        return PushNotificationResponse.from(notification);
    }

    @Transactional
    public void notify(
            Long profileId,
            PushNotificationType notificationType,
            String title,
            String body,
            String targetType,
            Long targetId
    ) {
        if (profileId == null || !getOrCreateSetting(profileId).isEnabled(notificationType)) {
            return;
        }
        List<PushDeviceToken> deviceTokens = pushDeviceTokenRepository.findByProfileId(profileId);
        PushDeliveryStatus status = deviceTokens.isEmpty()
                ? PushDeliveryStatus.STORED
                : PushDeliveryStatus.PENDING;
        PushNotification notification = pushNotificationRepository.save(PushNotification.create(
                profileId,
                notificationType,
                title,
                body,
                targetType,
                targetId,
                status
        ));
        if (!deviceTokens.isEmpty()) {
            FcmSendResult sendResult = fcmPushSender.send(notification, deviceTokens);
            if (sendResult.hasSuccess()) {
                notification.markSent();
            } else if (sendResult.hasFailure()) {
                notification.markFailed();
            }
        }
    }

    @Transactional
    public BroadcastPushNotificationResponse broadcast(BroadcastPushNotificationRequest request) {
        if (request == null || request.getNotificationType() == null) {
            throw new IllegalArgumentException("notificationType is required.");
        }
        if (request.getNotificationType() != PushNotificationType.EVENT_NOTICE
                && request.getNotificationType() != PushNotificationType.UPDATE_NOTICE) {
            throw new IllegalArgumentException("Broadcast notificationType must be EVENT_NOTICE or UPDATE_NOTICE.");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required.");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("body is required.");
        }
        List<Long> profileIds = profileRepository.findAll().stream()
                .map(profile -> profile.getId())
                .toList();
        profileIds.forEach(profileId -> notify(
                profileId,
                request.getNotificationType(),
                request.getTitle().trim(),
                request.getBody().trim(),
                request.getTargetType(),
                request.getTargetId()
        ));
        return new BroadcastPushNotificationResponse(request.getNotificationType(), profileIds.size());
    }

    private PushNotificationSetting getOrCreateSetting(Long profileId) {
        return pushNotificationSettingRepository.findByProfileId(profileId)
                .orElseGet(() -> pushNotificationSettingRepository.save(PushNotificationSetting.defaultSetting(profileId)));
    }

    private void validateProfile(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Profile not found. profileId=" + profileId);
        }
    }
}
