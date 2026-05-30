package com.example.kpick.profile.dto.res;

import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramInterestSearchResponse {
    private Long profileId;
    private String keyword;
    private List<ProgramInterestSearchItemResponse> programs;

    public static ProgramInterestSearchResponse from(Long profileId, String keyword, List<Program> programs, Set<Long> interestedProgramIds) {
        return new ProgramInterestSearchResponse(
                profileId,
                keyword,
                programs.stream()
                        .map(program -> ProgramInterestSearchItemResponse.from(program, interestedProgramIds.contains(program.getId())))
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramInterestSearchItemResponse {
        private Long programId;
        private String programName;
        private String broadcaster;
        private Genre genre;
        private String season;
        private Boolean isOnAir;
        private Boolean isInterested;

        public static ProgramInterestSearchItemResponse from(Program program, boolean isInterested) {
            return new ProgramInterestSearchItemResponse(
                    program.getId(),
                    program.getProgramName(),
                    program.getBroadcaster(),
                    program.getGenre(),
                    program.getSeason(),
                    program.isOnAir(),
                    isInterested
            );
        }
    }
}
