package com.example.kpick.community.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.thread.dto.req.CreateThreadRequest;
import com.example.kpick.community.uservote.dto.req.CreateUserVoteRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityPostRequest {
    private CommunityPostType postType;
    @JsonIgnore
    @Schema(hidden = true)
    private Long profileId;
    private Long programId;
    private Long missionId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
    private List<String> imageUrls;
    private LocalDateTime dueDateTime;
    private List<OptionRequest> options;

    public CreateCommunityPostRequest withProfileId(Long profileId) {
        return new CreateCommunityPostRequest(postType, profileId, programId, missionId, title, description, isNicknamePublic, imageUrls, dueDateTime, options);
    }

    public CreateThreadRequest toCreateThreadRequest() {
        return new CreateThreadRequest(profileId, programId, missionId, title, description, isNicknamePublic, imageUrls);
    }

    public CreateUserVoteRequest toCreateUserVoteRequest() {
        List<CreateUserVoteRequest.UserVoteOptionRequest> userVoteOptions = options == null ? null : options.stream()
                .map(option -> new CreateUserVoteRequest.UserVoteOptionRequest(option.getContent()))
                .toList();
        return new CreateUserVoteRequest(profileId, programId, title, description, dueDateTime, userVoteOptions);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        private String content;
    }
}
