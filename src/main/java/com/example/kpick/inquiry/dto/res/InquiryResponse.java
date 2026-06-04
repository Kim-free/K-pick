package com.example.kpick.inquiry.dto.res;

import com.example.kpick.inquiry.domain.Inquiry;
import com.example.kpick.inquiry.domain.InquiryStatus;
import com.example.kpick.inquiry.domain.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
    private Long inquiryId;
    private Long profileId;
    private InquiryType inquiryType;
    private String title;
    private List<String> imageUrls;
    private InquiryStatus inquiryStatus;
    private LocalDateTime createdAt;

    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getProfileId(),
                inquiry.getInquiryType(),
                inquiry.getTitle(),
                inquiry.getImageUrls(),
                inquiry.getInquiryStatus(),
                inquiry.getCreatedAt()
        );
    }
}
