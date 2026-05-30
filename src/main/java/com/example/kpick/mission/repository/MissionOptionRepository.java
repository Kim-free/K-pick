package com.example.kpick.mission.repository;

import com.example.kpick.mission.domain.MissionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionOptionRepository extends JpaRepository<MissionOption, Long> {
    List<MissionOption> findByMissionIdOrderByDisplayOrderAsc(Long missionId);
}
