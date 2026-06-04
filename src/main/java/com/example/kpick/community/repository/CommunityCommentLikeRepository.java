package com.example.kpick.community.repository;

import com.example.kpick.community.domain.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {
    Optional<CommunityCommentLike> findByCommunityCommentIdAndProfileId(Long communityCommentId, Long profileId);
    void deleteByCommunityCommentId(Long communityCommentId);
}
