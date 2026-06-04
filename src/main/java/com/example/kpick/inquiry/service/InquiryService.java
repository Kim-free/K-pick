package com.example.kpick.inquiry.service;

import com.example.kpick.inquiry.domain.Inquiry;
import com.example.kpick.inquiry.domain.InquiryStatus;
import com.example.kpick.inquiry.domain.InquiryType;
import com.example.kpick.inquiry.dto.req.CreateInquiryRequest;
import com.example.kpick.inquiry.dto.req.ReplyInquiryRequest;
import com.example.kpick.inquiry.dto.req.SaveInquiryDraftRequest;
import com.example.kpick.inquiry.dto.res.AdminInquiryResponse;
import com.example.kpick.inquiry.dto.res.InquiryResponse;
import com.example.kpick.inquiry.dto.res.ReplyInquiryResponse;
import com.example.kpick.inquiry.repository.InquiryRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final InquiryRepository inquiryRepository;
    private final InquiryEmailService inquiryEmailService;
    private final ProfileRepository profileRepository;

    @Transactional
    public InquiryResponse createInquiry(CreateInquiryRequest request) {
        validateCreateInquiryRequest(request);
        Inquiry inquiry = inquiryRepository.save(Inquiry.toEntity(request));
        return InquiryResponse.from(inquiry);
    }

    @Transactional(readOnly = true)
    public List<AdminInquiryResponse> getAdminInquiries(Boolean answered, InquiryType inquiryType) {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(inquiry -> answered == null
                        || answered == (inquiry.getInquiryStatus() == InquiryStatus.ANSWERED))
                .filter(inquiry -> inquiryType == null || inquiry.getInquiryType() == inquiryType)
                .map(this::toAdminInquiryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminInquiryResponse getAdminInquiry(Long inquiryId) {
        return toAdminInquiryResponse(findInquiry(inquiryId));
    }

    @Transactional
    public AdminInquiryResponse saveInquiryDraft(Long inquiryId, SaveInquiryDraftRequest request) {
        validateAnswerText(request == null ? null : request.getAnswerDraft(), "answerDraft");
        Inquiry inquiry = findInquiry(inquiryId);
        if (inquiry.getInquiryStatus() == InquiryStatus.ANSWERED) {
            throw new IllegalArgumentException("Answered inquiry cannot be changed to draft.");
        }
        inquiry.saveDraft(request.getAnswerDraft().trim());
        return toAdminInquiryResponse(inquiry);
    }

    @Transactional
    public ReplyInquiryResponse replyInquiry(Long inquiryId, ReplyInquiryRequest request) {
        validateAnswerText(request == null ? null : request.getAnswerContent(), "answerContent");
        Inquiry inquiry = findInquiry(inquiryId);
        boolean emailSent = inquiryEmailService.sendInquiryReply(
                inquiry.getReplyEmail(),
                inquiry.getTitle(),
                request.getAnswerContent().trim()
        );
        inquiry.answer(request.getAnswerContent().trim());
        return ReplyInquiryResponse.from(inquiry, emailSent);
    }

    private Inquiry findInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found."));
    }

    private AdminInquiryResponse toAdminInquiryResponse(Inquiry inquiry) {
        String nickname = profileRepository.findById(inquiry.getProfileId())
                .map(Profile::getNickname)
                .orElse(null);
        return AdminInquiryResponse.from(inquiry, nickname);
    }

    private void validateAnswerText(String answerText, String fieldName) {
        if (answerText == null || answerText.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
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
