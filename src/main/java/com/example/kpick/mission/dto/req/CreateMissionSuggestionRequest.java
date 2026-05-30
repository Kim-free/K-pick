package com.example.kpick.mission.dto.req;

import com.example.kpick.program.domain.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionSuggestionRequest {
    private Long profileId;
    private Long programId;
    private String episode;
    private Genre genre;
    private String missionTitle;
    private List<MissionSuggestionOptionRequest> options;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionSuggestionOptionRequest {
        private String content;
    }
}
