package com.example.kpick.community.uservote.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectUserVoteOptionRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private Long userVoteOptionId;

    public SelectUserVoteOptionRequest withProfileId(Long profileId) {
        return new SelectUserVoteOptionRequest(profileId, userVoteOptionId);
    }
}
