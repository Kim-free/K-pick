package com.example.kpick.report.controller;

import com.example.kpick.report.dto.req.CreateReportRequest;
import com.example.kpick.report.dto.res.CreateReportResponse;
import com.example.kpick.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<CreateReportResponse> createReport(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody CreateReportRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(request.withReporterProfileId(profileId)));
    }
}
