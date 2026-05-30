package com.example.kpick.benefit.domain;

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
@AllArgsConstructor @NoArgsConstructor @Builder
public class PickHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private PickHistoryType pickHistoryType;

    private long amount;
    private String description;
    private LocalDateTime createdAt;

    public static PickHistory create(Long profileId, String nickname, PickHistoryType pickHistoryType, long amount, String description) {
        return PickHistory.builder()
                .profileId(profileId)
                .nickname(nickname)
                .pickHistoryType(pickHistoryType)
                .amount(amount)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
