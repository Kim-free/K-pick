package com.example.kpick.community.repository;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.domain.CommunityPostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostViewRepository extends JpaRepository<CommunityPostView, Long> {
    boolean existsByPostTypeAndPostIdAndProfileId(CommunityPostType postType, Long postId, Long profileId);
}
