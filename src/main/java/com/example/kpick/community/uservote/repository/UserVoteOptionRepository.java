package com.example.kpick.community.uservote.repository;

import com.example.kpick.community.uservote.domain.UserVoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserVoteOptionRepository extends JpaRepository<UserVoteOption, Long> {
    List<UserVoteOption> findByUserVoteIdOrderByDisplayOrderAsc(Long userVoteId);
    void deleteByUserVoteId(Long userVoteId);
}
