package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.profile.domain.Profile;
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
    private String nickname;
    private String profileImageUrl;
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;

    public static CommunityCommentResponse from(CommunityComment comment, Profile profile) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPostType(),
                comment.getPostId(),
                comment.getProfileId(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getProfileImageUrl(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt()
        );
    }
}
