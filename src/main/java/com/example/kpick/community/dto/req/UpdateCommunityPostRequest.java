package com.example.kpick.community.dto.req;

import com.example.kpick.community.thread.dto.req.UpdateThreadRequest;
import com.example.kpick.community.uservote.dto.req.UpdateUserVoteRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityPostRequest {
    private Long programId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
    private LocalDateTime dueDateTime;
    private List<OptionRequest> options;

    public UpdateThreadRequest toUpdateThreadRequest() {
        return new UpdateThreadRequest(programId, title, description, isNicknamePublic);
    }

    public UpdateUserVoteRequest toUpdateUserVoteRequest() {
        List<UpdateUserVoteRequest.UserVoteOptionRequest> userVoteOptions = options == null ? null : options.stream()
                .map(option -> new UpdateUserVoteRequest.UserVoteOptionRequest(option.getContent()))
                .toList();
        return new UpdateUserVoteRequest(programId, title, description, dueDateTime, userVoteOptions);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        private String content;
    }
}
