package com.example.kpick.program.domain;

import com.example.kpick.program.dto.req.CreateProgramRequest;
import com.example.kpick.program.dto.req.UpdateProgramRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String programName;
    private String broadcaster;

    @Enumerated(EnumType.STRING)
    private Genre genre;
    
    private String season;
    private int episodeCount;
    private LocalDate broadcastStartDate;
    private LocalDate broadcastEndDate;
    private String thumbnailImageUrl;
    private boolean isOnAir;
    private boolean isExposed;
    private String description;
    private int missionCount;

    public static Program toEntity(CreateProgramRequest request) {
        return Program.builder()
                .programName(request.getProgramName().trim())
                .broadcaster(request.getBroadcaster().trim())
                .genre(request.getGenre())
                .season(request.getSeason() == null ? null : request.getSeason().trim())
                .episodeCount(request.getEpisodeCount() == null ? 0 : request.getEpisodeCount())
                .broadcastStartDate(request.getBroadcastStartDate())
                .broadcastEndDate(request.getBroadcastEndDate())
                .thumbnailImageUrl(request.getThumbnailImageUrl() == null ? null : request.getThumbnailImageUrl().trim())
                .isOnAir(request.getIsOnAir() == null ? calculateOnAir(request.getBroadcastEndDate()) : request.getIsOnAir())
                .isExposed(request.getIsExposed())
                .description(request.getDescription() == null ? null : request.getDescription().trim())
                .missionCount(0)
                .build();
    }

    public void update(UpdateProgramRequest request) {
        if (request.getProgramName() != null) {
            this.programName = request.getProgramName().trim();
        }
        if (request.getBroadcaster() != null) {
            this.broadcaster = request.getBroadcaster().trim();
        }
        if (request.getGenre() != null) {
            this.genre = request.getGenre();
        }
        if (request.getSeason() != null) {
            this.season = request.getSeason().trim();
        }
        if (request.getEpisodeCount() != null) {
            this.episodeCount = request.getEpisodeCount();
        }
        if (request.getBroadcastStartDate() != null) {
            this.broadcastStartDate = request.getBroadcastStartDate();
        }
        if (request.getBroadcastEndDate() != null) {
            this.broadcastEndDate = request.getBroadcastEndDate();
        }
        if (request.getThumbnailImageUrl() != null) {
            this.thumbnailImageUrl = request.getThumbnailImageUrl().trim();
        }
        if (request.getIsOnAir() != null) {
            this.isOnAir = request.getIsOnAir();
        }
        if (request.getIsExposed() != null) {
            this.isExposed = request.getIsExposed();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription().trim();
        }
    }

    private static boolean calculateOnAir(LocalDate broadcastEndDate) {
        return broadcastEndDate == null || !broadcastEndDate.isBefore(LocalDate.now());
    }
}
