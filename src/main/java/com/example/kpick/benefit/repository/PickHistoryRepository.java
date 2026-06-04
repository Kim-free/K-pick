package com.example.kpick.benefit.repository;

import com.example.kpick.benefit.domain.PickHistory;
import com.example.kpick.benefit.domain.PickHistoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickHistoryRepository extends JpaRepository<PickHistory, Long> {
    List<PickHistory> findAllByOrderByCreatedAtDesc();
    List<PickHistory> findByPickHistoryTypeOrderByCreatedAtDesc(PickHistoryType pickHistoryType);
    List<PickHistory> findByProfileIdOrderByCreatedAtDesc(Long profileId);
}
