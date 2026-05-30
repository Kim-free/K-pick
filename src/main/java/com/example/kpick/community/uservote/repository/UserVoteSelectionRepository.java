package com.example.kpick.community.uservote.repository;

import com.example.kpick.community.uservote.domain.UserVoteSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoteSelectionRepository extends JpaRepository<UserVoteSelection, Long> {
    boolean existsByUserVoteIdAndProfileId(Long userVoteId, Long profileId);
    Optional<UserVoteSelection> findByUserVoteIdAndProfileId(Long userVoteId, Long profileId);
    void deleteByUserVoteId(Long userVoteId);
}
