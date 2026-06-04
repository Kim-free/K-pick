package com.example.kpick.inquiry.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.kpick.inquiry.domain.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInquiryRequest {
    @JsonIgnore
    @Schema(hidden = true)
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

    public CreateInquiryRequest withProfileId(Long profileId) {
        return new CreateInquiryRequest(profileId, inquiryType, title, content, imageUrls, replyEmail, privacyAgreed);
    }
}
