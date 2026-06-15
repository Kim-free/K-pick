package com.example.kpick.profile.repository;

import com.example.kpick.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findAllByOrderByMissionPointDesc();
    List<Profile> findAllByOrderByActivityPointDesc();
    Optional<Profile> findByAppUserId(Long appUserId);
    Optional<Profile> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, Long profileId);
}
