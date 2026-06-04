package com.example.kpick.community.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityCommentRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private String content;

    public CreateCommunityCommentRequest withProfileId(Long profileId) {
        return new CreateCommunityCommentRequest(profileId, content);
    }
}
