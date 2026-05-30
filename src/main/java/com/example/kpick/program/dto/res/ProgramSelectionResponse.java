package com.example.kpick.program.dto.res;

import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSelectionResponse {
    private Long programId;
    private String programName;
    private Genre genre;
    private List<String> episodes;

    public static ProgramSelectionResponse from(Program program, List<String> episodes) {
        return new ProgramSelectionResponse(
                program.getId(),
                program.getProgramName(),
                program.getGenre(),
                episodes
        );
    }
}
