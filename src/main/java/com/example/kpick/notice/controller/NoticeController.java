package com.example.kpick.notice.controller;

import com.example.kpick.notice.dto.res.NoticeDetailsResponse;
import com.example.kpick.notice.dto.res.NoticeListResponse;
import com.example.kpick.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeListResponse>> getPublishedNotices() {
        return ResponseEntity.ok(noticeService.getPublishedNotices());
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailsResponse> getPublishedNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getPublishedNotice(noticeId));
    }
}
