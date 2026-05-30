package com.example.kpick.community.uservote.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserVoteRequest {
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
