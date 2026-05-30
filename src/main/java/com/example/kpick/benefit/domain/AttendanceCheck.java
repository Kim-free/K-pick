package com.example.kpick.benefit.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class AttendanceCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private LocalDate attendanceDate;

    public static AttendanceCheck create(Long profileId, LocalDate attendanceDate) {
        return AttendanceCheck.builder()
                .profileId(profileId)
                .attendanceDate(attendanceDate)
                .build();
    }
}
