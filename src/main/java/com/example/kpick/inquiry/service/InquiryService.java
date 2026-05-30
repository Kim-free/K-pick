package com.example.kpick.inquiry.service;

import com.example.kpick.inquiry.domain.Inquiry;
import com.example.kpick.inquiry.dto.req.CreateInquiryRequest;
import com.example.kpick.inquiry.dto.res.InquiryResponse;
import com.example.kpick.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final InquiryRepository inquiryRepository;

    @Transactional
    public InquiryResponse createInquiry(CreateInquiryRequest request) {
        validateCreateInquiryRequest(request);
        Inquiry inquiry = inquiryRepository.save(Inquiry.toEntity(request));
        return InquiryResponse.from(inquiry);
    }

    private void validateCreateInquiryRequest(CreateInquiryRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getInquiryType() == null) throw new IllegalArgumentException("inquiryType is required.");
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("content is required.");
        }
        if (request.getContent().trim().length() > 500) {
            throw new IllegalArgumentException("content must be 500 characters or less.");
        }
        if (request.getImageUrls() != null && request.getImageUrls().size() > 3) {
            throw new IllegalArgumentException("imageUrls can contain up to 3 images.");
        }
        if (request.getReplyEmail() == null || request.getReplyEmail().isBlank()) {
            throw new IllegalArgumentException("replyEmail is required.");
        }
        if (!EMAIL_PATTERN.matcher(request.getReplyEmail().trim()).matches()) {
            throw new IllegalArgumentException("replyEmail format is invalid.");
        }
        if (!request.getPrivacyAgreed()) {
            throw new IllegalArgumentException("privacyAgreed must be true.");
        }
    }
}
