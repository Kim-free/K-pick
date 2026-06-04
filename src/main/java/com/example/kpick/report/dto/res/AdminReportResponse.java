package com.example.kpick.report.dto.res;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.report.domain.Report;
import com.example.kpick.report.domain.ReportReason;
import com.example.kpick.report.domain.ReportStatus;
import com.example.kpick.report.domain.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportResponse {
    private Long reportId;
    private ReportTargetType targetType;
    private CommunityPostType postType;
    private Long targetId;
    private String targetSummary;
    private Long targetProfileId;
    private String targetNickname;
    private Long reporterProfileId;
    private String reporterNickname;
    private ReportReason reason;
    private String reasonDetail;
    private ReportStatus reportStatus;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static AdminReportResponse from(Report report, String targetNickname, String reporterNickname) {
        return new AdminReportResponse(
                report.getId(),
                report.getTargetType(),
                report.getPostType(),
                report.getTargetId(),
                report.getTargetSummary(),
                report.getTargetProfileId(),
                targetNickname,
                report.getReporterProfileId(),
                reporterNickname,
                report.getReason(),
                report.getReasonDetail(),
                report.getReportStatus(),
                report.getCreatedAt(),
                report.getProcessedAt()
        );
    }
}
