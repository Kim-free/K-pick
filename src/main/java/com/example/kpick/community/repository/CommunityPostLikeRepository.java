package com.example.kpick.community.repository;

import com.example.kpick.community.domain.CommunityPostLike;
import com.example.kpick.community.domain.CommunityPostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {
    Optional<CommunityPostLike> findByPostTypeAndPostIdAndProfileId(CommunityPostType postType, Long postId, Long profileId);
}
