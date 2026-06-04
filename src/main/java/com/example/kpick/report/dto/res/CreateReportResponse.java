package com.example.kpick.report.dto.res;

import com.example.kpick.report.domain.Report;
import com.example.kpick.report.domain.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportResponse {
    private Long reportId;
    private ReportStatus reportStatus;
    private LocalDateTime createdAt;

    public static CreateReportResponse from(Report report) {
        return new CreateReportResponse(report.getId(), report.getReportStatus(), report.getCreatedAt());
    }
}
