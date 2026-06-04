package com.example.kpick.inquiry.dto.res;

import com.example.kpick.inquiry.domain.Inquiry;
import com.example.kpick.inquiry.domain.InquiryStatus;
import com.example.kpick.inquiry.domain.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminInquiryResponse {
    private Long inquiryId;
    private Long profileId;
    private String nickname;
    private InquiryType inquiryType;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String replyEmail;
    private InquiryStatus inquiryStatus;
    private String answerDraft;
    private String answerContent;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    public static AdminInquiryResponse from(Inquiry inquiry, String nickname) {
        return new AdminInquiryResponse(
                inquiry.getId(),
                inquiry.getProfileId(),
                nickname,
                inquiry.getInquiryType(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getImageUrls(),
                inquiry.getReplyEmail(),
                inquiry.getInquiryStatus(),
                inquiry.getAnswerDraft(),
                inquiry.getAnswerContent(),
                inquiry.getCreatedAt(),
                inquiry.getAnsweredAt()
        );
    }
}
