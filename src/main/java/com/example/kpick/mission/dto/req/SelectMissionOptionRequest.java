package com.example.kpick.mission.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectMissionOptionRequest {
    private Long profileId;
    private Long missionOptionId;
}
