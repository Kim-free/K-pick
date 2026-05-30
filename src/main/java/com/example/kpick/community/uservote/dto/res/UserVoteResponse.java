package com.example.kpick.community.uservote.dto.res;

import com.example.kpick.community.uservote.domain.UserVote;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserVoteResponse {
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
    private LocalDateTime createdAt;

    public static UserVoteResponse from(UserVote userVote) {
        return new UserVoteResponse(
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
                userVote.getCreatedAt()
        );
    }
}
