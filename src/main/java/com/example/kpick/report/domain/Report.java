package com.example.kpick.report.domain;

import com.example.kpick.community.domain.CommunityPostType;
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
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterProfileId;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Enumerated(EnumType.STRING)
    private CommunityPostType postType;

    private Long targetId;
    private Long targetProfileId;
    private String targetSummary;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public void markProcessed() {
        this.reportStatus = ReportStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject() {
        this.reportStatus = ReportStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }
}
