package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionRecommendationResponse {
    private Long missionId;
    private Long programId;
    private String programName;
    private Genre genre;
    private String season;
    private String missionName;
    private LocalDateTime dueDateTime;
    private int coinFee;
    private int attenderCount;
    private MissionState missionState;
    private List<MissionOptionResponse> options;

    public static MissionRecommendationResponse from(Mission mission, Program program, List<MissionOption> options) {
        return new MissionRecommendationResponse(
                mission.getId(),
                mission.getProgramId(),
                program.getProgramName(),
                program.getGenre(),
                program.getSeason(),
                mission.getMissionName(),
                mission.getDueDateTime(),
                mission.getCoinFee(),
                mission.getAttenderCount(),
                mission.getMissionState(),
                options.stream()
                        .map(MissionOptionResponse::from)
                        .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionOptionResponse {
        private Long missionOptionId;
        private String content;
        private Integer displayOrder;

        public static MissionOptionResponse from(MissionOption option) {
            return new MissionOptionResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder()
            );
        }
    }
}
