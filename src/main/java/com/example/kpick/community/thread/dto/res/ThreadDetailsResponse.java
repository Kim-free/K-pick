package com.example.kpick.community.thread.dto.res;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.dto.res.CommunityCommentResponse;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.program.domain.Program;
import com.example.kpick.community.thread.domain.Thread;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ThreadDetailsResponse {
    private CommunityPostType postType;
    private Long threadId;
    private Long programId;
    private String programName;
    private Long profileId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
    private Boolean isSharedFromMission;
    private SharedMissionResponse sharedMission;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private List<CommunityCommentResponse> comments;
    private LocalDateTime createdAt;

    public static ThreadDetailsResponse from(Thread thread, Program program, Mission mission, List<CommunityCommentResponse> comments) {
        return new ThreadDetailsResponse(
                CommunityPostType.THREAD,
                thread.getId(),
                thread.getProgramId(),
                program.getProgramName(),
                thread.getProfileId(),
                thread.getTitle(),
                thread.getDescription(),
                thread.isNicknamePublic(),
                mission != null,
                mission == null ? null : SharedMissionResponse.from(mission, program),
                thread.getViewCount(),
                thread.getLikeCount(),
                thread.getCommentCount(),
                comments,
                thread.getCreatedAt()
        );
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SharedMissionResponse {
        private Long missionId;
        private String missionName;
        private Long programId;
        private String programName;

        public static SharedMissionResponse from(Mission mission, Program program) {
            return new SharedMissionResponse(
                    mission.getId(),
                    mission.getMissionName(),
                    mission.getProgramId(),
                    program.getProgramName()
            );
        }
    }
}
