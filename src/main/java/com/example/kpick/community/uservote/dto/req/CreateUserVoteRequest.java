package com.example.kpick.community.uservote.dto.req;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserVoteRequest {
    private Long profileId;
    private Long programId;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private List<UserVoteOptionRequest> options;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVoteOptionRequest {
        private String content;
    }
}
