package com.example.kpick.community.uservote.domain;

import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.uservote.dto.req.CreateUserVoteRequest;
import com.example.kpick.community.uservote.dto.req.UpdateUserVoteRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class UserVote extends CommunityPost {
    private String description;
    private LocalDateTime dueDateTime;
    private int voteCount;

    public static UserVote toEntity(CreateUserVoteRequest request) {
        UserVote userVote = UserVote.builder()
                .description(request.getDescription() == null ? null : request.getDescription().trim())
                .dueDateTime(request.getDueDateTime())
                .voteCount(0)
                .build();
        userVote.initializeCommunityPost(request.getProfileId(), request.getProgramId(), request.getTitle());
        return userVote;
    }

    public void increaseVoteCount() { this.voteCount++; }

    public void update(UpdateUserVoteRequest request) {
        if (request.getProgramId() != null) {
            this.programId = request.getProgramId();
        }
        if (request.getTitle() != null) {
            this.title = request.getTitle().trim();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription().trim();
        }
        if (request.getDueDateTime() != null) {
            this.dueDateTime = request.getDueDateTime();
        }
    }

    public void resetVoteCount() {
        this.voteCount = 0;
    }
}
