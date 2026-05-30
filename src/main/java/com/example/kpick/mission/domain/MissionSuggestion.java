package com.example.kpick.mission.domain;

import com.example.kpick.mission.dto.req.CreateMissionSuggestionRequest;
import com.example.kpick.program.domain.Genre;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class MissionSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private Long programId;
    private String episode;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    private String missionTitle;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private MissionSuggestionStatus status;

    public static MissionSuggestion toEntity(CreateMissionSuggestionRequest request) {
        return MissionSuggestion.builder()
                .profileId(request.getProfileId())
                .programId(request.getProgramId())
                .episode(request.getEpisode() == null ? null : request.getEpisode().trim())
                .genre(request.getGenre())
                .missionTitle(request.getMissionTitle().trim())
                .createdAt(LocalDateTime.now())
                .status(MissionSuggestionStatus.SUBMITTED)
                .build();
    }
}
