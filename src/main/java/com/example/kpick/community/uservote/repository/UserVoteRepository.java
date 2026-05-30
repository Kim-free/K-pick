package com.example.kpick.community.uservote.repository;

import com.example.kpick.community.uservote.domain.UserVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserVoteRepository extends JpaRepository<UserVote, Long> {
    List<UserVote> findAllByOrderByCreatedAtDesc();
    List<UserVote> findByProfileIdOrderByCreatedAtDesc(Long profileId);
    long countByProfileId(Long profileId);
}
