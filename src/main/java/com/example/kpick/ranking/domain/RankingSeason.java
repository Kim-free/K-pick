package com.example.kpick.ranking.domain;

import com.example.kpick.ranking.dto.req.CreateRankingSeasonRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingSeason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seasonName;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private int rewardTopN;
    private String rewardDescription;

    @Enumerated(EnumType.STRING)
    private RankingSeasonStatus seasonStatus;

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public static RankingSeason create(CreateRankingSeasonRequest request) {
        return RankingSeason.builder()
                .seasonName(request.getSeasonName().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rewardTopN(request.getRewardTopN())
                .rewardDescription(request.getRewardDescription().trim())
                .seasonStatus(RankingSeasonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void close() {
        this.seasonStatus = RankingSeasonStatus.ENDED;
        this.closedAt = LocalDateTime.now();
    }
}
