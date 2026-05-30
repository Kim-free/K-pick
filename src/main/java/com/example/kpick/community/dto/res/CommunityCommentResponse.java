package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCommentResponse {
    private Long communityCommentId;
    private CommunityPostType postType;
    private Long postId;
    private Long profileId;
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;

    public static CommunityCommentResponse from(CommunityComment comment) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPostType(),
                comment.getPostId(),
                comment.getProfileId(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt()
        );
    }
}
