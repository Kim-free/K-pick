package com.example.kpick.community.uservote.dto.req;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectUserVoteOptionRequest {
    private Long profileId;
    private Long userVoteOptionId;
}
