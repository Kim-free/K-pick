package com.example.kpick.admob.domain;

import com.example.kpick.admob.dto.req.AdMobSsvCallbackRequest;
import jakarta.persistence.Column;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdRewardTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String rawUserId;

    @Column(nullable = false, unique = true)
    private String transactionId;

    private String adNetwork;
    private String adUnit;
    private long rewardAmount;
    private String rewardItem;

    @Enumerated(EnumType.STRING)
    private AdRewardTransactionStatus status;

    private LocalDateTime createdAt;

    public static AdRewardTransaction completed(AdMobSsvCallbackRequest request) {
        return AdRewardTransaction.builder()
                .userId(request.getUserId())
                .rawUserId(request.getRawUserId())
                .transactionId(request.getTransactionId())
                .adNetwork(request.getAdNetwork())
                .adUnit(request.getAdUnit())
                .rewardAmount(request.getRewardAmount())
                .rewardItem(request.getRewardItem())
                .status(AdRewardTransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
