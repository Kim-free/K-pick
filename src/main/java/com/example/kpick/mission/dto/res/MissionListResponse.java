package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.program.domain.Program;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionListResponse {
    private Long missionId;
    private Long programId;
    private String programName;
    private String season;
    private String missionName;
    private int coinFee;
    private int attenderCount;
    private MissionState missionState;

    public static MissionListResponse from(Mission mission, Program program) {
        return new MissionListResponse(
                mission.getId(),
                mission.getProgramId(),
                program.getProgramName(),
                program.getSeason(),
                mission.getMissionName(),
                mission.getCoinFee(),
                mission.getAttenderCount(),
                mission.getMissionState()
        );
    }
}
