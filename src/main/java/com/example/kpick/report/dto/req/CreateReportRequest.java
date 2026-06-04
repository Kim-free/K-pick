package com.example.kpick.report.dto.req;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.report.domain.ReportReason;
import com.example.kpick.report.domain.ReportTargetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private Long reporterProfileId;
    private ReportTargetType targetType;
    private CommunityPostType postType;
    private Long targetId;
    private ReportReason reason;
    private String reasonDetail;

    public CreateReportRequest withReporterProfileId(Long reporterProfileId) {
        return new CreateReportRequest(reporterProfileId, targetType, postType, targetId, reason, reasonDetail);
    }
}
