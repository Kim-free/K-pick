package com.example.kpick.program.dto.res;

import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramEpisodeResponse {
    private Long programId;
    private String programName;
    private List<String> episodes;

    public static ProgramEpisodeResponse from(Program program, List<String> episodes) {
        return new ProgramEpisodeResponse(
                program.getId(),
                program.getProgramName(),
                episodes
        );
    }
}
