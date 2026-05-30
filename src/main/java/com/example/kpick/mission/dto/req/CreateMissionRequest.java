package com.example.kpick.mission.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionRequest {
    private Long programId;
    private Long profileId;
    private String missionName;
    private String episode;
    private LocalDateTime dueDateTime;
    private Integer coinFee;
    private List<MissionOptionRequest> options;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionOptionRequest {
        private String content;
    }
}
