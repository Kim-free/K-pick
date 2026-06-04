package com.example.kpick.report.repository;

import com.example.kpick.report.domain.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {
    List<UserSanction> findByProfileIdOrderByCreatedAtDesc(Long profileId);
}
