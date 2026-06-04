package com.example.kpick.notification.repository;

import com.example.kpick.notification.domain.PushDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushDeviceTokenRepository extends JpaRepository<PushDeviceToken, Long> {
    Optional<PushDeviceToken> findByDeviceToken(String deviceToken);
    List<PushDeviceToken> findByProfileId(Long profileId);
}
