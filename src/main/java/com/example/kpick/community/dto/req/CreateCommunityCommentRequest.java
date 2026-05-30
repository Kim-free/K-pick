package com.example.kpick.community.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityCommentRequest {
    private Long profileId;
    private String content;
}
