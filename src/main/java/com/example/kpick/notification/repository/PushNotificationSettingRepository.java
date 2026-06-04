package com.example.kpick.notification.repository;

import com.example.kpick.notification.domain.PushNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushNotificationSettingRepository extends JpaRepository<PushNotificationSetting, Long> {
    Optional<PushNotificationSetting> findByProfileId(Long profileId);
}
