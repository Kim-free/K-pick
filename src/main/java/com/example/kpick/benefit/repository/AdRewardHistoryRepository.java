package com.example.kpick.benefit.repository;

import com.example.kpick.benefit.domain.AdRewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AdRewardHistoryRepository extends JpaRepository<AdRewardHistory, Long> {
    long countByProfileIdAndRewardDate(Long profileId, LocalDate rewardDate);
}
