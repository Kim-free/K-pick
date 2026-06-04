package com.example.kpick.report.controller;

import com.example.kpick.report.domain.ReportStatus;
import com.example.kpick.report.domain.ReportTargetType;
import com.example.kpick.report.dto.req.CreateUserSanctionRequest;
import com.example.kpick.report.dto.res.AdminReportResponse;
import com.example.kpick.report.dto.res.UserSanctionResponse;
import com.example.kpick.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class ReportAdminController {
    private final ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportResponse>> getAdminReports(
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(required = false) ReportStatus reportStatus
    ) {
        return ResponseEntity.ok(reportService.getAdminReports(targetType, reportStatus));
    }

    @PostMapping("/users/{profileId}/sanctions")
    public ResponseEntity<UserSanctionResponse> createUserSanction(
            @PathVariable Long profileId,
            @RequestAttribute("authenticatedProfileId") Long createdByProfileId,
            @RequestBody CreateUserSanctionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createUserSanction(profileId, request.withCreatedByProfileId(createdByProfileId)));
    }
}
