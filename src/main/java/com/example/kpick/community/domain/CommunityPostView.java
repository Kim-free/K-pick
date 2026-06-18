package com.example.kpick.community.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_type", "post_id", "profile_id"})
})
public class CommunityPostView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private CommunityPostType postType;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public static CommunityPostView create(CommunityPostType postType, Long postId, Long profileId) {
        return CommunityPostView.builder()
                .postType(postType)
                .postId(postId)
                .profileId(profileId)
                .viewedAt(LocalDateTime.now())
                .build();
    }
}
