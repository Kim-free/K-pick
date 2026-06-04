package com.example.kpick.community.domain;

import com.example.kpick.community.dto.req.CreateCommunityCommentRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class CommunityComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CommunityPostType postType;

    private Long postId;
    private Long profileId;
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;

    @Enumerated(EnumType.STRING)
    private CommunityContentStatus contentStatus;

    public static CommunityComment toEntity(CommunityPostType postType, Long postId, CreateCommunityCommentRequest request) {
        return CommunityComment.builder()
                .postType(postType)
                .postId(postId)
                .profileId(request.getProfileId())
                .content(request.getContent().trim())
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .contentStatus(CommunityContentStatus.NORMAL)
                .build();
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public boolean isVisible() {
        return this.contentStatus == null || this.contentStatus == CommunityContentStatus.NORMAL;
    }

    public void updateContentStatus(CommunityContentStatus contentStatus) {
        this.contentStatus = contentStatus;
    }
}
