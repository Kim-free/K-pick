package com.example.kpick.community.thread.dto.res;

import com.example.kpick.program.domain.Program;
import com.example.kpick.community.thread.domain.Thread;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ThreadResponse {
    private Long threadId;
    private Long programId;
    private String programName;
    private Long profileId;
    private Long missionId;
    private String title;
    private String description;
    private Boolean isNicknamePublic;
    private Boolean isSharedFromMission;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static ThreadResponse from(Thread thread, Program program) {
        return new ThreadResponse(
                thread.getId(),
                thread.getProgramId(),
                program.getProgramName(),
                thread.getProfileId(),
                thread.getMissionId(),
                thread.getTitle(),
                thread.getDescription(),
                thread.isNicknamePublic(),
                thread.getMissionId() != null,
                thread.getViewCount(),
                thread.getLikeCount(),
                thread.getCommentCount(),
                thread.getCreatedAt()
        );
    }
}
