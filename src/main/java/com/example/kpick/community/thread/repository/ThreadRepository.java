package com.example.kpick.community.thread.repository;

import com.example.kpick.community.thread.domain.Thread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThreadRepository extends JpaRepository<Thread, Long> {
    List<Thread> findAllByOrderByCreatedAtDesc();
    List<Thread> findByProfileIdOrderByCreatedAtDesc(Long profileId);
    long countByProfileId(Long profileId);
}
