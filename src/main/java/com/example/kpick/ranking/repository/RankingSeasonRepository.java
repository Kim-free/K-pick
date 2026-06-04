package com.example.kpick.ranking.repository;

import com.example.kpick.ranking.domain.RankingSeason;
import com.example.kpick.ranking.domain.RankingSeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankingSeasonRepository extends JpaRepository<RankingSeason, Long> {
    List<RankingSeason> findAllByOrderByStartDateDesc();
    Optional<RankingSeason> findFirstBySeasonStatus(RankingSeasonStatus seasonStatus);
    boolean existsBySeasonStatus(RankingSeasonStatus seasonStatus);
}
