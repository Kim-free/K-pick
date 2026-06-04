package com.example.kpick.notice.controller;

import com.example.kpick.notice.domain.NoticeStatus;
import com.example.kpick.notice.dto.req.CreateNoticeRequest;
import com.example.kpick.notice.dto.req.UpdateNoticeStatusRequest;
import com.example.kpick.notice.dto.res.NoticeDetailsResponse;
import com.example.kpick.notice.dto.res.NoticeListResponse;
import com.example.kpick.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class NoticeAdminController {
    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<NoticeDetailsResponse> createNotice(@RequestBody CreateNoticeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.createNotice(request));
    }

    @GetMapping
    public ResponseEntity<List<NoticeListResponse>> getAdminNotices(
            @RequestParam(required = false) NoticeStatus noticeStatus
    ) {
        return ResponseEntity.ok(noticeService.getAdminNotices(noticeStatus));
    }

    @PatchMapping("/{noticeId}/status")
    public ResponseEntity<NoticeDetailsResponse> updateNoticeStatus(
            @PathVariable Long noticeId,
            @RequestBody UpdateNoticeStatusRequest request
    ) {
        return ResponseEntity.ok(noticeService.updateNoticeStatus(noticeId, request));
    }
}
