package com.example.kpick.ranking.repository;

import com.example.kpick.ranking.domain.SeasonReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonRewardRepository extends JpaRepository<SeasonReward, Long> {
    List<SeasonReward> findByRankingSeasonIdOrderByRankAsc(Long rankingSeasonId);
    boolean existsByRankingSeasonId(Long rankingSeasonId);
}
