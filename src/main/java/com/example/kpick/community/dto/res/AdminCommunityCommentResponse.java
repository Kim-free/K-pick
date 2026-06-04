package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityContentStatus;
import com.example.kpick.community.domain.CommunityPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminCommunityCommentResponse {
    private Long communityCommentId;
    private CommunityPostType postType;
    private Long postId;
    private Long profileId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private long reportCount;
    private CommunityContentStatus contentStatus;

    public static AdminCommunityCommentResponse from(CommunityComment comment, String nickname, long reportCount) {
        return new AdminCommunityCommentResponse(
                comment.getId(),
                comment.getPostType(),
                comment.getPostId(),
                comment.getProfileId(),
                nickname,
                comment.getContent(),
                comment.getCreatedAt(),
                reportCount,
                comment.getContentStatus() == null ? CommunityContentStatus.NORMAL : comment.getContentStatus()
        );
    }
}
