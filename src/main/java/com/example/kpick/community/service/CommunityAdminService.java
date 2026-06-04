package com.example.kpick.community.service;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityContentStatus;
import com.example.kpick.community.domain.CommunityPost;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.dto.req.UpdateCommunityContentStatusRequest;
import com.example.kpick.community.dto.res.AdminCommunityCommentResponse;
import com.example.kpick.community.dto.res.AdminCommunityPostDetailsResponse;
import com.example.kpick.community.dto.res.AdminCommunityPostResponse;
import com.example.kpick.community.repository.CommunityCommentLikeRepository;
import com.example.kpick.community.repository.CommunityCommentRepository;
import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.repository.UserVoteOptionRepository;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.report.domain.Report;
import com.example.kpick.report.domain.ReportStatus;
import com.example.kpick.report.domain.ReportTargetType;
import com.example.kpick.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CommunityAdminService {
    private final CommunityService communityService;
    private final ThreadRepository threadRepository;
    private final UserVoteRepository userVoteRepository;
    private final UserVoteOptionRepository userVoteOptionRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityCommentLikeRepository communityCommentLikeRepository;
    private final ProfileRepository profileRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public List<AdminCommunityPostResponse> getPosts(
            CommunityPostType postType,
            String keyword,
            CommunityContentStatus contentStatus,
            Integer minReportCount
    ) {
        return getPostStream(postType)
                .map(entry -> toPostResponse(entry.getPostType(), entry.getPost()))
                .filter(response -> keyword == null || keyword.isBlank()
                        || response.getTitle().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(response -> contentStatus == null || response.getContentStatus() == contentStatus)
                .filter(response -> minReportCount == null || response.getReportCount() >= minReportCount)
                .sorted(Comparator.comparing(AdminCommunityPostResponse::getCreatedAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCommunityPostDetailsResponse getPostDetails(CommunityPostType postType, Long postId) {
        return switch (postType) {
            case THREAD -> {
                Thread thread = findThread(postId);
                yield AdminCommunityPostDetailsResponse.fromThread(thread, getNickname(thread.getProfileId()), countPostReports(postType, postId));
            }
            case USER_VOTE -> {
                UserVote userVote = findUserVote(postId);
                yield AdminCommunityPostDetailsResponse.fromUserVote(
                        userVote,
                        getNickname(userVote.getProfileId()),
                        countPostReports(postType, postId),
                        userVoteOptionRepository.findByUserVoteIdOrderByDisplayOrderAsc(postId)
                );
            }
        };
    }

    @Transactional
    public AdminCommunityPostResponse updatePostStatus(
            CommunityPostType postType,
            Long postId,
            UpdateCommunityContentStatusRequest request
    ) {
        validateStatusRequest(request);
        CommunityPost post = findPost(postType, postId);
        post.updateContentStatus(request.getContentStatus());
        return toPostResponse(postType, post);
    }

    @Transactional
    public void deletePost(CommunityPostType postType, Long postId) {
        switch (postType) {
            case THREAD -> communityService.deleteThread(postId);
            case USER_VOTE -> communityService.deleteUserVote(postId);
        }
    }

    @Transactional(readOnly = true)
    public List<AdminCommunityCommentResponse> getComments(
            String keyword,
            CommunityContentStatus contentStatus,
            Integer minReportCount
    ) {
        return communityCommentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toCommentResponse)
                .filter(response -> keyword == null || keyword.isBlank()
                        || response.getContent().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(response -> contentStatus == null || response.getContentStatus() == contentStatus)
                .filter(response -> minReportCount == null || response.getReportCount() >= minReportCount)
                .toList();
    }

    @Transactional
    public AdminCommunityCommentResponse updateCommentStatus(Long communityCommentId, UpdateCommunityContentStatusRequest request) {
        validateStatusRequest(request);
        CommunityComment comment = findComment(communityCommentId);
        comment.updateContentStatus(request.getContentStatus());
        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long communityCommentId) {
        CommunityComment comment = findComment(communityCommentId);
        findPost(comment.getPostType(), comment.getPostId()).decreaseCommentCount();
        communityCommentLikeRepository.deleteByCommunityCommentId(communityCommentId);
        communityCommentRepository.delete(comment);
    }

    @Transactional
    public void rejectPostReports(CommunityPostType postType, Long postId) {
        findPost(postType, postId);
        reportRepository.findByTargetTypeAndPostTypeAndTargetIdAndReportStatus(
                ReportTargetType.POST, postType, postId, ReportStatus.WAITING
        ).forEach(Report::reject);
    }

    @Transactional
    public void rejectCommentReports(Long communityCommentId) {
        findComment(communityCommentId);
        reportRepository.findByTargetTypeAndTargetIdAndReportStatus(
                ReportTargetType.COMMENT, communityCommentId, ReportStatus.WAITING
        ).forEach(Report::reject);
    }

    private Stream<PostEntry> getPostStream(CommunityPostType postType) {
        Stream<PostEntry> threadStream = threadRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(thread -> new PostEntry(CommunityPostType.THREAD, thread));
        Stream<PostEntry> userVoteStream = userVoteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(userVote -> new PostEntry(CommunityPostType.USER_VOTE, userVote));
        if (postType == CommunityPostType.THREAD) return threadStream;
        if (postType == CommunityPostType.USER_VOTE) return userVoteStream;
        return Stream.concat(threadStream, userVoteStream);
    }

    private AdminCommunityPostResponse toPostResponse(CommunityPostType postType, CommunityPost post) {
        return AdminCommunityPostResponse.from(postType, post, getNickname(post.getProfileId()), countPostReports(postType, post.getId()));
    }

    private AdminCommunityCommentResponse toCommentResponse(CommunityComment comment) {
        long reportCount = reportRepository.countByTargetTypeAndTargetId(ReportTargetType.COMMENT, comment.getId());
        return AdminCommunityCommentResponse.from(comment, getNickname(comment.getProfileId()), reportCount);
    }

    private long countPostReports(CommunityPostType postType, Long postId) {
        return reportRepository.countByTargetTypeAndPostTypeAndTargetId(ReportTargetType.POST, postType, postId);
    }

    private CommunityPost findPost(CommunityPostType postType, Long postId) {
        return switch (postType) {
            case THREAD -> findThread(postId);
            case USER_VOTE -> findUserVote(postId);
        };
    }

    private Thread findThread(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found. threadId=" + threadId));
    }

    private UserVote findUserVote(Long userVoteId) {
        return userVoteRepository.findById(userVoteId)
                .orElseThrow(() -> new IllegalArgumentException("User vote not found. userVoteId=" + userVoteId));
    }

    private CommunityComment findComment(Long communityCommentId) {
        return communityCommentRepository.findById(communityCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Community comment not found. communityCommentId=" + communityCommentId));
    }

    private String getNickname(Long profileId) {
        return profileRepository.findById(profileId).map(Profile::getNickname).orElse(null);
    }

    private void validateStatusRequest(UpdateCommunityContentStatusRequest request) {
        if (request == null || request.getContentStatus() == null) {
            throw new IllegalArgumentException("contentStatus is required.");
        }
    }

    @Getter
    @AllArgsConstructor
    private static class PostEntry {
        private CommunityPostType postType;
        private CommunityPost post;
    }
}
