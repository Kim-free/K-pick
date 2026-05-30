package com.example.kpick.program.dto.req;

import com.example.kpick.program.domain.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProgramRequest {
    private String programName;
    private String broadcaster;
    private Genre genre;
    private String season;
    private Integer episodeCount;
    private LocalDate broadcastStartDate;
    private LocalDate broadcastEndDate;
    private String thumbnailImageUrl;
    private Boolean isOnAir;
    private Boolean isExposed;
    private String description;
}
