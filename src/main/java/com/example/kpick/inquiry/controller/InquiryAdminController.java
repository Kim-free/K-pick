package com.example.kpick.inquiry.controller;

import com.example.kpick.inquiry.domain.InquiryType;
import com.example.kpick.inquiry.dto.req.ReplyInquiryRequest;
import com.example.kpick.inquiry.dto.req.SaveInquiryDraftRequest;
import com.example.kpick.inquiry.dto.res.AdminInquiryResponse;
import com.example.kpick.inquiry.dto.res.ReplyInquiryResponse;
import com.example.kpick.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/admin/inquiries")
public class InquiryAdminController {
    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<List<AdminInquiryResponse>> getAdminInquiries(
            @RequestParam(required = false) Boolean answered,
            @RequestParam(required = false) InquiryType inquiryType
    ) {
        return ResponseEntity.ok(inquiryService.getAdminInquiries(answered, inquiryType));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<AdminInquiryResponse> getAdminInquiry(@PathVariable Long inquiryId) {
        return ResponseEntity.ok(inquiryService.getAdminInquiry(inquiryId));
    }

    @PatchMapping("/{inquiryId}/draft")
    public ResponseEntity<AdminInquiryResponse> saveInquiryDraft(
            @PathVariable Long inquiryId,
            @RequestBody SaveInquiryDraftRequest request
    ) {
        return ResponseEntity.ok(inquiryService.saveInquiryDraft(inquiryId, request));
    }

    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<ReplyInquiryResponse> replyInquiry(
            @PathVariable Long inquiryId,
            @RequestBody ReplyInquiryRequest request
    ) {
        return ResponseEntity.ok(inquiryService.replyInquiry(inquiryId, request));
    }
}
