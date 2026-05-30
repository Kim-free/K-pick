package com.example.kpick.community.uservote.dto.res;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.dto.res.CommunityCommentResponse;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.domain.UserVoteOption;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserVoteDetailsResponse {
    private CommunityPostType postType;
    private Long userVoteId;
    private Long profileId;
    private Long programId;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private int voteCount;
    private UserVoteOptionResponse selectedOption;
    private List<UserVoteOptionResponse> options;
    private List<CommunityCommentResponse> comments;
    private LocalDateTime createdAt;

    public static UserVoteDetailsResponse from(
            UserVote userVote,
            List<UserVoteOption> options,
            UserVoteOption selectedOption,
            List<CommunityCommentResponse> comments
    ) {
        return new UserVoteDetailsResponse(
                CommunityPostType.USER_VOTE,
                userVote.getId(),
                userVote.getProfileId(),
                userVote.getProgramId(),
                userVote.getTitle(),
                userVote.getDescription(),
                userVote.getDueDateTime(),
                userVote.getViewCount(),
                userVote.getLikeCount(),
                userVote.getCommentCount(),
                userVote.getVoteCount(),
                selectedOption == null ? null : UserVoteOptionResponse.from(selectedOption),
                options.stream()
                        .map(UserVoteOptionResponse::from)
                        .toList(),
                comments,
                userVote.getCreatedAt()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVoteOptionResponse {
        private Long userVoteOptionId;
        private String content;
        private Integer displayOrder;
        private int voteCount;
        private int voteRate;

        public static UserVoteOptionResponse from(UserVoteOption option) {
            int totalVoteCount = option.getUserVote().getVoteCount();
            int voteRate = totalVoteCount == 0 ? 0 : (int) Math.round((option.getVoteCount() * 100.0) / totalVoteCount);
            return new UserVoteOptionResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder(),
                    option.getVoteCount(),
                    voteRate
            );
        }
    }
}
