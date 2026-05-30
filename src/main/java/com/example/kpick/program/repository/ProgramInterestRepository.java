package com.example.kpick.program.repository;

import com.example.kpick.program.domain.ProgramInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramInterestRepository extends JpaRepository<ProgramInterest, Long> {
    List<ProgramInterest> findByProfileId(Long profileId);
    void deleteByProfileId(Long profileId);
}
