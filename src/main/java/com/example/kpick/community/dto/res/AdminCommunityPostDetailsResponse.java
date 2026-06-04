package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityContentStatus;
import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.domain.UserVoteOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminCommunityPostDetailsResponse {
    private CommunityPostType postType;
    private Long postId;
    private Long profileId;
    private String nickname;
    private Long programId;
    private Long missionId;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private int voteCount;
    private long reportCount;
    private CommunityContentStatus contentStatus;
    private List<UserVoteOptionResponse> userVoteOptions;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static AdminCommunityPostDetailsResponse fromThread(Thread thread, String nickname, long reportCount) {
        return from(CommunityPostType.THREAD, thread, nickname, thread.getMissionId(), thread.getDescription(),
                null, 0, reportCount, List.of(), thread.getImageUrls());
    }

    public static AdminCommunityPostDetailsResponse fromUserVote(
            UserVote userVote,
            String nickname,
            long reportCount,
            List<UserVoteOption> options
    ) {
        return from(CommunityPostType.USER_VOTE, userVote, nickname, null, userVote.getDescription(),
                userVote.getDueDateTime(), userVote.getVoteCount(), reportCount,
                options.stream().map(UserVoteOptionResponse::from).toList(), List.of());
    }

    private static AdminCommunityPostDetailsResponse from(
            CommunityPostType postType,
            CommunityPost post,
            String nickname,
            Long missionId,
            String description,
            LocalDateTime dueDateTime,
            int voteCount,
            long reportCount,
            List<UserVoteOptionResponse> options,
            List<String> imageUrls
    ) {
        return new AdminCommunityPostDetailsResponse(
                postType, post.getId(), post.getProfileId(), nickname, post.getProgramId(), missionId,
                post.getTitle(), description, dueDateTime, post.getViewCount(), post.getLikeCount(),
                post.getCommentCount(), voteCount, reportCount,
                post.getContentStatus() == null ? CommunityContentStatus.NORMAL : post.getContentStatus(),
                options, imageUrls, post.getCreatedAt()
        );
    }

    @Getter
    @AllArgsConstructor
    public static class UserVoteOptionResponse {
        private Long userVoteOptionId;
        private String content;
        private int displayOrder;
        private int voteCount;

        public static UserVoteOptionResponse from(UserVoteOption option) {
            return new UserVoteOptionResponse(option.getId(), option.getContent(), option.getDisplayOrder(), option.getVoteCount());
        }
    }
}
