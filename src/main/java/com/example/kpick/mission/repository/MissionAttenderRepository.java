package com.example.kpick.mission.repository;

import com.example.kpick.mission.domain.MissionAttender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionAttenderRepository extends JpaRepository<MissionAttender, Long> {
    boolean existsByMissionIdAndProfileId(Long missionId, Long profileId);
    Optional<MissionAttender> findByMissionIdAndProfileId(Long missionId, Long profileId);
    List<MissionAttender> findByMissionId(Long missionId);
    List<MissionAttender> findByMissionIdAndMissionOptionId(Long missionId, Long missionOptionId);
    List<MissionAttender> findByProfileId(Long profileId);
    long countByMissionIdAndMissionOptionId(Long missionId, Long missionOptionId);
    long countByProfileId(Long profileId);
}
