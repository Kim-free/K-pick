package com.example.kpick.notice.dto.req;

import com.example.kpick.notice.domain.NoticeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNoticeStatusRequest {
    private NoticeStatus noticeStatus;
}
