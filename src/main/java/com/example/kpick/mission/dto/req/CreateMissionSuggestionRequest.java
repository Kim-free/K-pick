package com.example.kpick.mission.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.kpick.program.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionSuggestionRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private Long programId;
    private String episode;
    private Genre genre;
    private String missionTitle;
    private List<MissionSuggestionOptionRequest> options;

    public CreateMissionSuggestionRequest withProfileId(Long profileId) {
        return new CreateMissionSuggestionRequest(profileId, programId, episode, genre, missionTitle, options);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissionSuggestionOptionRequest {
        private String content;
    }
}
