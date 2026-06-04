package com.example.kpick.mission.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private String missionName;
    private String episode;
    private LocalDateTime dueDateTime;
    private Integer coinFee;
    private Boolean isActive;
    private List<MissionOptionRequest> options;

    public CreateMissionRequest withProfileId(Long profileId) {
        return new CreateMissionRequest(programId, profileId, missionName, episode, dueDateTime, coinFee, isActive, options);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionOptionRequest {
        private String content;
    }
}
