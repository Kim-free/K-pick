package com.example.kpick.notice.domain;

import com.example.kpick.notice.dto.req.CreateNoticeRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private NoticeStatus noticeStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Notice toEntity(CreateNoticeRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return Notice.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .noticeStatus(request.getNoticeStatus() == null ? NoticeStatus.PUBLISHED : request.getNoticeStatus())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateStatus(NoticeStatus noticeStatus) {
        this.noticeStatus = noticeStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
