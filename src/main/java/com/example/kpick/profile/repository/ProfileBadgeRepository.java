package com.example.kpick.profile.repository;

import com.example.kpick.profile.domain.ProfileBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileBadgeRepository extends JpaRepository<ProfileBadge, Long> {
    List<ProfileBadge> findByProfileId(Long profileId);
    Optional<ProfileBadge> findByProfileIdAndBadgeCode(Long profileId, String badgeCode);
}
