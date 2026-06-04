package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityContentStatus;
import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.domain.CommunityPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminCommunityPostResponse {
    private CommunityPostType postType;
    private Long postId;
    private Long profileId;
    private String nickname;
    private Long programId;
    private String title;
    private LocalDateTime createdAt;
    private long reportCount;
    private CommunityContentStatus contentStatus;

    public static AdminCommunityPostResponse from(
            CommunityPostType postType,
            CommunityPost post,
            String nickname,
            long reportCount
    ) {
        return new AdminCommunityPostResponse(
                postType,
                post.getId(),
                post.getProfileId(),
                nickname,
                post.getProgramId(),
                post.getTitle(),
                post.getCreatedAt(),
                reportCount,
                post.getContentStatus() == null ? CommunityContentStatus.NORMAL : post.getContentStatus()
        );
    }
}
