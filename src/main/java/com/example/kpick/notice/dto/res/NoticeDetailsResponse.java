package com.example.kpick.notice.dto.res;

import com.example.kpick.notice.domain.Notice;
import com.example.kpick.notice.domain.NoticeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeDetailsResponse {
    private Long noticeId;
    private String title;
    private String content;
    private NoticeStatus noticeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoticeDetailsResponse from(Notice notice) {
        return new NoticeDetailsResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getNoticeStatus(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
