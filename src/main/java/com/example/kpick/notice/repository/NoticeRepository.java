package com.example.kpick.notice.repository;

import com.example.kpick.notice.domain.Notice;
import com.example.kpick.notice.domain.NoticeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByCreatedAtDesc();
    List<Notice> findByNoticeStatusOrderByCreatedAtDesc(NoticeStatus noticeStatus);
    Optional<Notice> findByIdAndNoticeStatus(Long noticeId, NoticeStatus noticeStatus);
}
