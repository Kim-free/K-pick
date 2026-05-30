package com.example.kpick.mission.dto.res;

import com.example.kpick.mission.domain.MissionSuggestion;
import com.example.kpick.mission.domain.MissionSuggestionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionSuggestionResponse {
    private Long missionSuggestionId;
    private MissionSuggestionStatus status;

    public static MissionSuggestionResponse from(MissionSuggestion suggestion) {
        return new MissionSuggestionResponse(
                suggestion.getId(),
                suggestion.getStatus()
        );
    }
}
