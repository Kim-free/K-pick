package com.example.kpick.report.service;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.repository.CommunityCommentRepository;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.report.domain.Report;
import com.example.kpick.report.domain.ReportStatus;
import com.example.kpick.report.domain.ReportTargetType;
import com.example.kpick.report.domain.UserSanction;
import com.example.kpick.report.dto.req.CreateReportRequest;
import com.example.kpick.report.dto.req.CreateUserSanctionRequest;
import com.example.kpick.report.dto.res.AdminReportResponse;
import com.example.kpick.report.dto.res.CreateReportResponse;
import com.example.kpick.report.dto.res.UserSanctionResponse;
import com.example.kpick.report.repository.ReportRepository;
import com.example.kpick.report.repository.UserSanctionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final ProfileRepository profileRepository;
    private final ThreadRepository threadRepository;
    private final UserVoteRepository userVoteRepository;
    private final CommunityCommentRepository communityCommentRepository;

    @Transactional
    public CreateReportResponse createReport(CreateReportRequest request) {
        validateCreateReportRequest(request);
        findProfile(request.getReporterProfileId());
        TargetSnapshot target = resolveTarget(request);
        if (target.getProfileId().equals(request.getReporterProfileId())) {
            throw new IllegalArgumentException("Cannot report yourself.");
        }

        Report report = reportRepository.save(Report.builder()
                .reporterProfileId(request.getReporterProfileId())
                .targetType(request.getTargetType())
                .postType(request.getPostType())
                .targetId(request.getTargetId())
                .targetProfileId(target.getProfileId())
                .targetSummary(target.getSummary())
                .reason(request.getReason())
                .reasonDetail(trimToNull(request.getReasonDetail()))
                .reportStatus(ReportStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build());

        return CreateReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<AdminReportResponse> getAdminReports(ReportTargetType targetType, ReportStatus reportStatus) {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(report -> targetType == null || report.getTargetType() == targetType)
                .filter(report -> reportStatus == null || report.getReportStatus() == reportStatus)
                .map(report -> AdminReportResponse.from(
                        report,
                        getNickname(report.getTargetProfileId()),
                        getNickname(report.getReporterProfileId())
                ))
                .toList();
    }

    @Transactional
    public UserSanctionResponse createUserSanction(Long profileId, CreateUserSanctionRequest request) {
        validateCreateUserSanctionRequest(request);
        findProfile(profileId);
        findProfile(request.getCreatedByProfileId());

        Report relatedReport = request.getRelatedReportId() == null
                ? null
                : findReport(request.getRelatedReportId());
        if (relatedReport != null && !relatedReport.getTargetProfileId().equals(profileId)) {
            throw new IllegalArgumentException("Report target does not match sanctioned profile.");
        }

        LocalDateTime startsAt = LocalDateTime.now();
        UserSanction sanction = userSanctionRepository.save(UserSanction.builder()
                .profileId(profileId)
                .relatedReportId(request.getRelatedReportId())
                .createdByProfileId(request.getCreatedByProfileId())
                .sanctionType(request.getSanctionType())
                .reason(request.getReason().trim())
                .notifyUser(Boolean.TRUE.equals(request.getNotifyUser()))
                .startsAt(startsAt)
                .endsAt(request.getSanctionType().calculateEndsAt(startsAt))
                .createdAt(startsAt)
                .build());
        if (relatedReport != null) {
            relatedReport.markProcessed();
        }

        return UserSanctionResponse.from(sanction);
    }

    private TargetSnapshot resolveTarget(CreateReportRequest request) {
        return switch (request.getTargetType()) {
            case POST -> resolvePostTarget(request.getPostType(), request.getTargetId());
            case COMMENT -> resolveCommentTarget(request.getTargetId());
            case USER -> {
                Profile profile = findProfile(request.getTargetId());
                yield new TargetSnapshot(profile.getId(), profile.getNickname());
            }
        };
    }

    private TargetSnapshot resolvePostTarget(CommunityPostType postType, Long postId) {
        if (postType == null) {
            throw new IllegalArgumentException("postType is required when targetType is POST.");
        }
        CommunityPost post = switch (postType) {
            case THREAD -> threadRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("Thread not found. threadId=" + postId));
            case USER_VOTE -> userVoteRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("User vote not found. userVoteId=" + postId));
        };
        return new TargetSnapshot(post.getProfileId(), post.getTitle());
    }

    private TargetSnapshot resolveCommentTarget(Long communityCommentId) {
        CommunityComment comment = communityCommentRepository.findById(communityCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Community comment not found. communityCommentId=" + communityCommentId));
        return new TargetSnapshot(comment.getProfileId(), comment.getContent());
    }

    private void validateCreateReportRequest(CreateReportRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getReporterProfileId() == null) throw new IllegalArgumentException("reporterProfileId is required.");
        if (request.getTargetType() == null) throw new IllegalArgumentException("targetType is required.");
        if (request.getTargetId() == null) throw new IllegalArgumentException("targetId is required.");
        if (request.getReason() == null) throw new IllegalArgumentException("reason is required.");
    }

    private void validateCreateUserSanctionRequest(CreateUserSanctionRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getCreatedByProfileId() == null) throw new IllegalArgumentException("createdByProfileId is required.");
        if (request.getSanctionType() == null) throw new IllegalArgumentException("sanctionType is required.");
        if (request.getReason() == null || request.getReason().isBlank()) throw new IllegalArgumentException("reason is required.");
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found. reportId=" + reportId));
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private String getNickname(Long profileId) {
        return profileRepository.findById(profileId)
                .map(Profile::getNickname)
                .orElse(null);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Getter
    @AllArgsConstructor
    private static class TargetSnapshot {
        private Long profileId;
        private String summary;
    }
}
