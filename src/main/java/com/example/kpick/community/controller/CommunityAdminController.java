package com.example.kpick.community.controller;

import com.example.kpick.community.domain.CommunityContentStatus;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.dto.req.UpdateCommunityContentStatusRequest;
import com.example.kpick.community.dto.res.AdminCommunityCommentResponse;
import com.example.kpick.community.dto.res.AdminCommunityPostDetailsResponse;
import com.example.kpick.community.dto.res.AdminCommunityPostResponse;
import com.example.kpick.community.service.CommunityAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/community")
public class CommunityAdminController {
    private final CommunityAdminService communityAdminService;

    @GetMapping("/posts")
    public ResponseEntity<List<AdminCommunityPostResponse>> getPosts(
            @RequestParam(required = false) CommunityPostType postType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CommunityContentStatus contentStatus,
            @RequestParam(required = false) Integer minReportCount
    ) {
        return ResponseEntity.ok(communityAdminService.getPosts(postType, keyword, contentStatus, minReportCount));
    }

    @GetMapping("/posts/{postType}/{postId}")
    public ResponseEntity<AdminCommunityPostDetailsResponse> getPostDetails(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(communityAdminService.getPostDetails(postType, postId));
    }

    @PatchMapping("/posts/{postType}/{postId}/status")
    public ResponseEntity<AdminCommunityPostResponse> updatePostStatus(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId,
            @RequestBody UpdateCommunityContentStatusRequest request
    ) {
        return ResponseEntity.ok(communityAdminService.updatePostStatus(postType, postId, request));
    }

    @DeleteMapping("/posts/{postType}/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable CommunityPostType postType, @PathVariable Long postId) {
        communityAdminService.deletePost(postType, postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/posts/{postType}/{postId}/reports/reject")
    public ResponseEntity<Void> rejectPostReports(@PathVariable CommunityPostType postType, @PathVariable Long postId) {
        communityAdminService.rejectPostReports(postType, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<List<AdminCommunityCommentResponse>> getComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CommunityContentStatus contentStatus,
            @RequestParam(required = false) Integer minReportCount
    ) {
        return ResponseEntity.ok(communityAdminService.getComments(keyword, contentStatus, minReportCount));
    }

    @PatchMapping("/comments/{communityCommentId}/status")
    public ResponseEntity<AdminCommunityCommentResponse> updateCommentStatus(
            @PathVariable Long communityCommentId,
            @RequestBody UpdateCommunityContentStatusRequest request
    ) {
        return ResponseEntity.ok(communityAdminService.updateCommentStatus(communityCommentId, request));
    }

    @DeleteMapping("/comments/{communityCommentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long communityCommentId) {
        communityAdminService.deleteComment(communityCommentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/comments/{communityCommentId}/reports/reject")
    public ResponseEntity<Void> rejectCommentReports(@PathVariable Long communityCommentId) {
        communityAdminService.rejectCommentReports(communityCommentId);
        return ResponseEntity.noContent().build();
    }
}
