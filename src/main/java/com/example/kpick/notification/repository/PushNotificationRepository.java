package com.example.kpick.notification.repository;

import com.example.kpick.notification.domain.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findByProfileIdOrderByCreatedAtDesc(Long profileId);
}
