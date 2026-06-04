package com.example.kpick.program.dto.res;

import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {
    private Long programId;
    private String programName;
    private String broadcaster;
    private Genre genre;
    private String season;
    private int episodeCount;
    private LocalDate broadcastStartDate;
    private LocalDate broadcastEndDate;
    private LocalDate registeredDate;
    private String thumbnailImageUrl;
    private Boolean isOnAir;
    private Boolean isExposed;
    private String description;
    private int missionCount;

    public static ProgramResponse from(Program program) {
        return new ProgramResponse(
                program.getId(),
                program.getProgramName(),
                program.getBroadcaster(),
                program.getGenre(),
                program.getSeason(),
                program.getEpisodeCount(),
                program.getBroadcastStartDate(),
                program.getBroadcastEndDate(),
                program.getRegisteredDate(),
                program.getThumbnailImageUrl(),
                program.isOnAir(),
                program.isExposed(),
                program.getDescription(),
                program.getMissionCount()
        );
    }
}
