package com.example.kpick.report.repository;

import com.example.kpick.report.domain.Report;
import com.example.kpick.report.domain.ReportStatus;
import com.example.kpick.report.domain.ReportTargetType;
import com.example.kpick.community.domain.CommunityPostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByOrderByCreatedAtDesc();
    long countByTargetTypeAndPostTypeAndTargetId(ReportTargetType targetType, CommunityPostType postType, Long targetId);
    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
    List<Report> findByTargetTypeAndPostTypeAndTargetIdAndReportStatus(
            ReportTargetType targetType,
            CommunityPostType postType,
            Long targetId,
            ReportStatus reportStatus
    );
    List<Report> findByTargetTypeAndTargetIdAndReportStatus(
            ReportTargetType targetType,
            Long targetId,
            ReportStatus reportStatus
    );
}
