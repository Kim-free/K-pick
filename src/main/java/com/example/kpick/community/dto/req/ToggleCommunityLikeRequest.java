package com.example.kpick.community.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ToggleCommunityLikeRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;

    public ToggleCommunityLikeRequest withProfileId(Long profileId) {
        return new ToggleCommunityLikeRequest(profileId);
    }
}
