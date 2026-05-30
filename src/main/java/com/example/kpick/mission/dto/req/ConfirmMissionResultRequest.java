package com.example.kpick.mission.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.example.kpick.mission.domain.ResultPublishTiming;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmMissionResultRequest {
    private Long correctMissionOptionId;
    private ResultPublishTiming resultPublishTiming;
}
