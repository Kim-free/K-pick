package com.example.kpick.mission.repository;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findAllByOrderByIdDesc();
    List<Mission> findByProgramIdOrderByDueDateTimeDesc(Long programId);
    List<Mission> findByMissionStateOrderByDueDateTimeAsc(MissionState missionState);
    List<Mission> findByMissionStateAndProgramIdInOrderByIdDesc(MissionState missionState, List<Long> programIds);
    List<Mission> findByMissionStateAndProgramIdOrderByIdDesc(MissionState missionState, Long programId);
    long countByProfileId(Long profileId);
    long countByProgramId(Long programId);
}
