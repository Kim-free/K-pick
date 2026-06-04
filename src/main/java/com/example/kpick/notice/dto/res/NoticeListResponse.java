package com.example.kpick.notice.dto.res;

import com.example.kpick.notice.domain.Notice;
import com.example.kpick.notice.domain.NoticeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NoticeListResponse {
    private Long noticeId;
    private String title;
    private NoticeStatus noticeStatus;
    private LocalDateTime createdAt;

    public static NoticeListResponse from(Notice notice) {
        return new NoticeListResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getNoticeStatus(),
                notice.getCreatedAt()
        );
    }
}
