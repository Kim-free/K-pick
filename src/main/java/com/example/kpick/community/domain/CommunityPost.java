package com.example.kpick.community.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected Long profileId;
    protected Long programId;
    protected String title;
    protected LocalDateTime createdAt;
    protected int viewCount;
    protected int likeCount;
    protected int commentCount;

    protected void initializeCommunityPost(Long profileId, Long programId, String title) {
        this.profileId = profileId;
        this.programId = programId;
        this.title = title.trim();
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }
}
