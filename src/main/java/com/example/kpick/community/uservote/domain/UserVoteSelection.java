package com.example.kpick.community.uservote.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class UserVoteSelection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vote_id", nullable = false)
    private UserVote userVote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vote_option_id", nullable = false)
    private UserVoteOption userVoteOption;

    private Long profileId;
}
