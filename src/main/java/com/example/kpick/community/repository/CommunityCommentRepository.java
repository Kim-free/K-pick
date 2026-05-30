package com.example.kpick.community.repository;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityPostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByPostTypeAndPostIdOrderByCreatedAtAsc(CommunityPostType postType, Long postId);
    List<CommunityComment> findByProfileIdOrderByCreatedAtDesc(Long profileId);
    long countByProfileId(Long profileId);
}
