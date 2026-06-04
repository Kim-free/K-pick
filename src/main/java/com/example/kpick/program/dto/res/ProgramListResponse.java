package com.example.kpick.program.dto.res;

import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramListResponse {
    private Long programId;
    private String programName;
    private Genre genre;
    private String thumbnailImageUrl;

    public static ProgramListResponse from(Program program) {
        return new ProgramListResponse(
                program.getId(),
                program.getProgramName(),
                program.getGenre(),
                program.getThumbnailImageUrl()
        );
    }
}
