package com.example.kpick.benefit.repository;

import com.example.kpick.benefit.domain.AttendanceCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheck, Long> {
    boolean existsByProfileIdAndAttendanceDate(Long profileId, LocalDate attendanceDate);
    List<AttendanceCheck> findByProfileIdAndAttendanceDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);
    List<AttendanceCheck> findByProfileIdAndAttendanceDateLessThanEqualOrderByAttendanceDateDesc(Long profileId, LocalDate attendanceDate);
}
