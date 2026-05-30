package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.domain.ResultPublishTiming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponse {
    private Long missionId;
    private Long programId;
    private Long profileId;
    private String missionName;
    private String episode;
    private LocalDateTime dueDateTime;
    private int coinFee;
    private int attenderCount;
    private ResultPublishTiming resultPublishTiming;
    private MissionState missionState;
    private List<MissionOptionResponse> options;

    public static MissionResponse from(Mission mission, List<MissionOption> options) {
        return new MissionResponse(
                mission.getId(),
                mission.getProgramId(),
                mission.getProfileId(),
                mission.getMissionName(),
                mission.getEpisode(),
                mission.getDueDateTime(),
                mission.getCoinFee(),
                mission.getAttenderCount(),
                mission.getResultPublishTiming(),
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
        private Boolean isCorrect;

        public static MissionOptionResponse from(MissionOption option) {
            return new MissionOptionResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder(),
                    option.getIsCorrect()
            );
        }
    }
}
