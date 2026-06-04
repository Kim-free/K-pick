package com.example.kpick.inquiry.domain;

import com.example.kpick.inquiry.dto.req.CreateInquiryRequest;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;

    @Enumerated(EnumType.STRING)
    private InquiryType inquiryType;

    private String title;
    private String content;

    @ElementCollection
    @CollectionTable(name = "inquiry_image_url", joinColumns = @JoinColumn(name = "inquiry_id"))
    private List<String> imageUrls;

    private String replyEmail;
    private boolean privacyAgreed;

    @Enumerated(EnumType.STRING)
    private InquiryStatus inquiryStatus;

    private String answerDraft;
    private String answerContent;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    public static Inquiry toEntity(CreateInquiryRequest request) {
        return Inquiry.builder()
                .profileId(request.getProfileId())
                .inquiryType(request.getInquiryType())
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .imageUrls(request.getImageUrls() == null ? List.of() : request.getImageUrls())
                .replyEmail(request.getReplyEmail().trim())
                .privacyAgreed(request.getPrivacyAgreed())
                .inquiryStatus(InquiryStatus.RECEIVED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void saveDraft(String answerDraft) {
        this.answerDraft = answerDraft;
        this.inquiryStatus = InquiryStatus.DRAFTED;
    }

    public void answer(String answerContent) {
        this.answerDraft = null;
        this.answerContent = answerContent;
        this.inquiryStatus = InquiryStatus.ANSWERED;
        this.answeredAt = LocalDateTime.now();
    }
}
