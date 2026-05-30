package com.example.kpick.inquiry.controller;

import com.example.kpick.inquiry.dto.req.CreateInquiryRequest;
import com.example.kpick.inquiry.dto.res.InquiryResponse;
import com.example.kpick.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<InquiryResponse> createInquiry(@RequestBody CreateInquiryRequest request) {
        return ResponseEntity.ok(inquiryService.createInquiry(request));
    }
}
