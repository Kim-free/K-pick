package com.example.kpick.notification.repository;

import com.example.kpick.notification.domain.PushNotification;
import com.example.kpick.notification.domain.PushNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    @Query("""
            select count(pushNotification) > 0
            from PushNotification pushNotification
            where pushNotification.profileId = :profileId
              and pushNotification.notificationType = :notificationType
              and pushNotification.targetType = :targetType
              and pushNotification.targetId = :targetId
            """)
    boolean existsSameTargetNotification(
            @Param("profileId") Long profileId,
            @Param("notificationType") PushNotificationType notificationType,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    boolean existsByProfileIdAndNotificationTypeAndTargetTypeAndCreatedAtBetween(
            Long profileId,
            PushNotificationType notificationType,
            String targetType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
