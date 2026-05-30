package com.example.kpick.community.uservote.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class UserVoteOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_vote_id", nullable = false)
    private UserVote userVote;

    private String content;
    private Integer displayOrder;
    private int voteCount;

    public void increaseVoteCount() { this.voteCount++; }
}
