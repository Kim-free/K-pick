package com.example.kpick.program.repository;

import com.example.kpick.program.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByIsOnAir(boolean isOnAir);
}
