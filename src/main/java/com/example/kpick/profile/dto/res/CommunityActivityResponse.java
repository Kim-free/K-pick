package com.example.kpick.profile.dto.res;

import com.example.kpick.community.domain.CommunityPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityActivityResponse {
    private Long profileId;
    private String activityType;
    private long totalCount;
    private List<CommunityActivityItemResponse> activities;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityActivityItemResponse {
        private String activityTargetType;
        private CommunityPostType postType;
        private Long postId;
        private Long communityCommentId;
        private String title;
        private String content;
        private int likeCount;
        private int commentCount;
        private LocalDateTime createdAt;
    }
}
