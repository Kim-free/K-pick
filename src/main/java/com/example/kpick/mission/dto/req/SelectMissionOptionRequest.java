package com.example.kpick.mission.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectMissionOptionRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private Long missionOptionId;

    public SelectMissionOptionRequest withProfileId(Long profileId) {
        return new SelectMissionOptionRequest(profileId, missionOptionId);
    }
}
