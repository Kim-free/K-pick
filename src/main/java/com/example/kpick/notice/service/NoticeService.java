package com.example.kpick.notice.service;

import com.example.kpick.notice.domain.Notice;
import com.example.kpick.notice.domain.NoticeStatus;
import com.example.kpick.notice.dto.req.CreateNoticeRequest;
import com.example.kpick.notice.dto.req.UpdateNoticeStatusRequest;
import com.example.kpick.notice.dto.res.NoticeDetailsResponse;
import com.example.kpick.notice.dto.res.NoticeListResponse;
import com.example.kpick.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    @Transactional
    public NoticeDetailsResponse createNotice(CreateNoticeRequest request) {
        validateCreateRequest(request);
        return NoticeDetailsResponse.from(noticeRepository.save(Notice.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<NoticeListResponse> getAdminNotices(NoticeStatus noticeStatus) {
        List<Notice> notices = noticeStatus == null
                ? noticeRepository.findAllByOrderByCreatedAtDesc()
                : noticeRepository.findByNoticeStatusOrderByCreatedAtDesc(noticeStatus);
        return notices.stream().map(NoticeListResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NoticeListResponse> getPublishedNotices() {
        return noticeRepository.findByNoticeStatusOrderByCreatedAtDesc(NoticeStatus.PUBLISHED).stream()
                .map(NoticeListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeDetailsResponse getPublishedNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndNoticeStatus(noticeId, NoticeStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("Published notice not found."));
        return NoticeDetailsResponse.from(notice);
    }

    @Transactional
    public NoticeDetailsResponse updateNoticeStatus(Long noticeId, UpdateNoticeStatusRequest request) {
        if (request == null || request.getNoticeStatus() == null) {
            throw new IllegalArgumentException("noticeStatus is required.");
        }
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("Notice not found."));
        notice.updateStatus(request.getNoticeStatus());
        return NoticeDetailsResponse.from(notice);
    }

    private void validateCreateRequest(CreateNoticeRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required.");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("content is required.");
        }
    }
}
