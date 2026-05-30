package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionDetailsResponse {
    private Long missionId;
    private Long programId;
    private String programName;
    private String missionName;
    private LocalDateTime dueDateTime;
    private int coinFee;
    private Long profileCoin;
    private int attenderCount;
    private MissionState missionState;
    private boolean resultConfirmed;
    private List<MissionOptionResponse> options;

    public static MissionDetailsResponse from(Mission mission, Program program, Long profileCoin, List<MissionOption> options) {
        return new MissionDetailsResponse(
                mission.getId(),
                mission.getProgramId(),
                program.getProgramName(),
                mission.getMissionName(),
                mission.getDueDateTime(),
                mission.getCoinFee(),
                profileCoin,
                mission.getAttenderCount(),
                mission.getMissionState(),
                false,
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
