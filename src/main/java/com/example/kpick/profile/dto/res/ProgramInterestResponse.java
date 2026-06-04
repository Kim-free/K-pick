package com.example.kpick.profile.dto.res;

import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.domain.ProgramInterest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProgramInterestResponse {
    private Long profileId;
    private List<InterestProgramResponse> programs;

    public static ProgramInterestResponse from(Long profileId, List<ProgramInterest> interests) {
        return new ProgramInterestResponse(
                profileId,
                interests.stream()
                        .map(interest -> InterestProgramResponse.from(interest.getProgram()))
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestProgramResponse {
        private Long programId;
        private String programName;
        private String broadcaster;
        private Genre genre;
        private String season;
        private Boolean isOnAir;
        private String thumbnailImageUrl;

        public static InterestProgramResponse from(Program program) {
            return new InterestProgramResponse(
                    program.getId(),
                    program.getProgramName(),
                    program.getBroadcaster(),
                    program.getGenre(),
                    program.getSeason(),
                    program.isOnAir(),
                    program.getThumbnailImageUrl()
            );
        }
    }
}
