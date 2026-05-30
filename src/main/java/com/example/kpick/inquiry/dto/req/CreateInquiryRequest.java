package com.example.kpick.inquiry.dto.req;

import com.example.kpick.inquiry.domain.InquiryType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateInquiryRequest {
    private Long profileId;
    private InquiryType inquiryType;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String replyEmail;
    private Boolean privacyAgreed;

    public boolean getPrivacyAgreed() {
        return Boolean.TRUE.equals(privacyAgreed);
    }
}
