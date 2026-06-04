package com.example.kpick.inquiry.dto.res;

import com.example.kpick.inquiry.domain.Inquiry;
import com.example.kpick.inquiry.domain.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReplyInquiryResponse {
    private Long inquiryId;
    private InquiryStatus inquiryStatus;
    private String replyEmail;
    private boolean emailSent;
    private LocalDateTime answeredAt;

    public static ReplyInquiryResponse from(Inquiry inquiry, boolean emailSent) {
        return new ReplyInquiryResponse(
                inquiry.getId(),
                inquiry.getInquiryStatus(),
                inquiry.getReplyEmail(),
                emailSent,
                inquiry.getAnsweredAt()
        );
    }
}
