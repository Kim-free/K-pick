package com.example.kpick.community.dto.res;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.domain.UserVoteOption;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.program.domain.Program;
import com.example.kpick.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostResponse {
    private CommunityPostType postType;
    private Long postId;
    private Long profileId;
    private String nickname;
    private String profileImageUrl;
    private Long programId;
    private Long missionId;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private int voteCount;
    private Boolean isVoted;
    private Long selectedUserVoteOptionId;
    private List<UserVoteOptionSummaryResponse> userVoteOptions;
    private SharedMissionSummaryResponse sharedMission;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static CommunityPostResponse fromThread(Thread thread) {
        return fromThread(thread, null, null, null);
    }

    public static CommunityPostResponse fromThread(Thread thread, Mission mission, Program missionProgram, Profile profile) {
        return new CommunityPostResponse(
                CommunityPostType.THREAD,
                thread.getId(),
                thread.getProfileId(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getProfileImageUrl(),
                thread.getProgramId(),
                thread.getMissionId(),
                thread.getTitle(),
                thread.getDescription(),
                null,
                thread.getViewCount(),
                thread.getLikeCount(),
                thread.getCommentCount(),
                0,
                null,
                null,
                List.of(),
                mission == null ? null : SharedMissionSummaryResponse.from(mission, missionProgram),
                thread.getImageUrls(),
                thread.getCreatedAt()
        );
    }

    public static CommunityPostResponse fromUserVote(UserVote userVote) {
        return fromUserVote(userVote, List.of(), null, null);
    }

    public static CommunityPostResponse fromUserVote(
            UserVote userVote,
            List<UserVoteOption> options,
            Long selectedUserVoteOptionId,
            Profile profile
    ) {
        return new CommunityPostResponse(
                CommunityPostType.USER_VOTE,
                userVote.getId(),
                userVote.getProfileId(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getProfileImageUrl(),
                userVote.getProgramId(),
                null,
                userVote.getTitle(),
                userVote.getDescription(),
                userVote.getDueDateTime(),
                userVote.getViewCount(),
                userVote.getLikeCount(),
                userVote.getCommentCount(),
                userVote.getVoteCount(),
                selectedUserVoteOptionId != null,
                selectedUserVoteOptionId,
                options.stream()
                        .map(option -> UserVoteOptionSummaryResponse.from(option, selectedUserVoteOptionId))
                        .toList(),
                null,
                List.of(),
                userVote.getCreatedAt()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVoteOptionSummaryResponse {
        private Long userVoteOptionId;
        private String content;
        private Integer displayOrder;
        private Boolean isSelected;

        public static UserVoteOptionSummaryResponse from(UserVoteOption option, Long selectedUserVoteOptionId) {
            return new UserVoteOptionSummaryResponse(
                    option.getId(),
                    option.getContent(),
                    option.getDisplayOrder(),
                    selectedUserVoteOptionId != null && option.getId().equals(selectedUserVoteOptionId)
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SharedMissionSummaryResponse {
        private Long missionId;
        private Long programId;
        private String programName;
        private String episode;
        private String missionName;
        private LocalDateTime dueDateTime;
        private int attenderCount;

        public static SharedMissionSummaryResponse from(Mission mission, Program program) {
            return new SharedMissionSummaryResponse(
                    mission.getId(),
                    mission.getProgramId(),
                    program == null ? null : program.getProgramName(),
                    mission.getEpisode(),
                    mission.getMissionName(),
                    mission.getDueDateTime(),
                    mission.getAttenderCount()
            );
        }
    }
}
