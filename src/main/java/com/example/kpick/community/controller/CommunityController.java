package com.example.kpick.community.controller;

import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.dto.req.CreateCommunityCommentRequest;
import com.example.kpick.community.dto.req.CreateCommunityPostRequest;
import com.example.kpick.community.dto.req.ToggleCommunityLikeRequest;
import com.example.kpick.community.dto.req.UpdateCommunityPostRequest;
import com.example.kpick.community.dto.res.CommunityCommentResponse;
import com.example.kpick.community.dto.res.CommunityLikeToggleResponse;
import com.example.kpick.community.dto.res.CommunityPostResponse;
import com.example.kpick.community.service.CommunityService;
import com.example.kpick.community.uservote.dto.req.SelectUserVoteOptionRequest;
import com.example.kpick.community.uservote.dto.res.UserVoteDetailsResponse;
import com.example.kpick.program.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping("/api/community")
    public ResponseEntity<List<CommunityPostResponse>> getCommunityPosts(
            @RequestParam(required = false) CommunityPostType postType,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long profileId
    ) {
        return ResponseEntity.ok(communityService.getCommunityPosts(postType, genre, programId, profileId));
    }

    @PostMapping("/api/community")
    public ResponseEntity<CommunityPostResponse> createCommunityPost(@RequestBody CreateCommunityPostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createCommunityPost(request));
    }

    @GetMapping("/api/community/posts/{postId}")
    public ResponseEntity<Object> getCommunityPostDetails(
            @PathVariable Long postId,
            @RequestParam CommunityPostType postType,
            @RequestParam(required = false) Long profileId
    ) {
        return ResponseEntity.ok(communityService.getCommunityPostDetails(postType, postId, profileId));
    }

    @PatchMapping("/api/community/{postType}/{postId}")
    public ResponseEntity<CommunityPostResponse> updateCommunityPost(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId,
            @RequestBody UpdateCommunityPostRequest request
    ) {
        return ResponseEntity.ok(communityService.updateCommunityPost(postType, postId, request));
    }

    @DeleteMapping("/api/community/{postType}/{postId}")
    public ResponseEntity<Void> deleteCommunityPost(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId
    ) {
        communityService.deleteCommunityPost(postType, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/community/{postType}/{postId}/likes")
    public ResponseEntity<CommunityLikeToggleResponse> toggleCommunityPostLike(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId,
            @RequestBody ToggleCommunityLikeRequest request
    ) {
        return ResponseEntity.ok(communityService.togglePostLike(postType, postId, request));
    }

    @PostMapping("/api/community/{postType}/{postId}/comments")
    public ResponseEntity<CommunityCommentResponse> createCommunityComment(
            @PathVariable CommunityPostType postType,
            @PathVariable Long postId,
            @RequestBody CreateCommunityCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createComment(postType, postId, request));
    }

    @PostMapping("/api/community/{postType}/comments/{communityCommentId}/likes")
    public ResponseEntity<CommunityLikeToggleResponse> toggleCommunityCommentLike(
            @PathVariable CommunityPostType postType,
            @PathVariable Long communityCommentId,
            @RequestBody ToggleCommunityLikeRequest request
    ) {
        return ResponseEntity.ok(communityService.toggleCommentLike(postType, communityCommentId, request));
    }

    @PostMapping("/api/user-votes/{userVoteId}/vote")
    public ResponseEntity<UserVoteDetailsResponse> voteUserVote(@PathVariable Long userVoteId, @RequestBody SelectUserVoteOptionRequest request) {
        return ResponseEntity.ok(communityService.voteUserVote(userVoteId, request));
    }

    @GetMapping("/api/user-votes/{userVoteId}/result")
    public ResponseEntity<UserVoteDetailsResponse> getUserVoteResult(
            @PathVariable Long userVoteId,
            @RequestParam(required = false) Long profileId
    ) {
        return ResponseEntity.ok(communityService.getUserVoteResult(userVoteId, profileId));
    }
}
