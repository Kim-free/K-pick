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
public class AdminProgramListResponse {
    private Long programId;
    private String programName;
    private String season;
    private String broadcaster;
    private Genre genre;
    private int episodeCount;
    private long missionCount;
    private Boolean isOnAir;
    private Boolean isExposed;
    private LocalDate registeredDate;
    private String thumbnailImageUrl;

    public static AdminProgramListResponse from(Program program, long missionCount) {
        return new AdminProgramListResponse(
                program.getId(),
                program.getProgramName(),
                program.getSeason(),
                program.getBroadcaster(),
                program.getGenre(),
                program.getEpisodeCount(),
                missionCount,
                program.isOnAir(),
                program.isExposed(),
                program.getRegisteredDate(),
                program.getThumbnailImageUrl()
        );
    }
}
