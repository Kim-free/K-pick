package com.example.kpick.mission.repository;

import com.example.kpick.mission.domain.MissionSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionSuggestionRepository extends JpaRepository<MissionSuggestion, Long> {
}
