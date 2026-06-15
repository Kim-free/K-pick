package com.example.kpick.profile.controller;

import com.example.kpick.profile.dto.req.CreateProfileBadgeRequest;
import com.example.kpick.profile.dto.req.CompleteOnboardingProfileRequest;
import com.example.kpick.profile.dto.req.UpdateNicknameRequest;
import com.example.kpick.profile.dto.req.UpdateProfileImageRequest;
import com.example.kpick.profile.dto.req.UpdateProgramInterestsRequest;
import com.example.kpick.profile.dto.res.CommunityActivityResponse;
import com.example.kpick.profile.dto.res.MissionHistoryResponse;
import com.example.kpick.profile.dto.res.MyPageResponse;
import com.example.kpick.profile.dto.res.NicknameCheckResponse;
import com.example.kpick.profile.dto.res.OnboardingProfileResponse;
import com.example.kpick.profile.dto.res.ProfileBadgeResponse;
import com.example.kpick.profile.dto.res.ProfileNicknameResponse;
import com.example.kpick.profile.dto.res.ProfileImageResponse;
import com.example.kpick.profile.dto.res.ProgramInterestResponse;
import com.example.kpick.profile.dto.res.ProgramInterestSearchResponse;
import com.example.kpick.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me/my-page")
    public ResponseEntity<MyPageResponse> getMyPage(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(profileService.getMyPage(profileId));
    }

    @PatchMapping("/me/onboarding")
    public ResponseEntity<OnboardingProfileResponse> completeOnboardingProfile(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody CompleteOnboardingProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.completeOnboardingProfile(profileId, request));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<ProfileNicknameResponse> updateNickname(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(profileService.updateNickname(profileId, request));
    }

    @PatchMapping("/me/image")
    public ResponseEntity<ProfileImageResponse> updateProfileImage(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody UpdateProfileImageRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfileImage(profileId, request));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(profileService.checkNickname(nickname));
    }

    @GetMapping("/me/mission-history")
    public ResponseEntity<MissionHistoryResponse> getMissionHistory(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestParam(required = false) String resultFilter
    ) {
        return ResponseEntity.ok(profileService.getMissionHistory(profileId, resultFilter));
    }

    @GetMapping("/me/community-activities")
    public ResponseEntity<CommunityActivityResponse> getCommunityActivities(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestParam(required = false) String activityType
    ) {
        return ResponseEntity.ok(profileService.getCommunityActivities(profileId, activityType));
    }

    @GetMapping("/me/badges")
    public ResponseEntity<ProfileBadgeResponse> getBadges(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(profileService.getBadges(profileId));
    }

    @PostMapping("/me/badges")
    public ResponseEntity<ProfileBadgeResponse.ProfileBadgeItemResponse> createBadge(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody CreateProfileBadgeRequest request
    ) {
        return ResponseEntity.ok(profileService.createBadge(profileId, request));
    }

    @GetMapping("/me/program-interests")
    public ResponseEntity<ProgramInterestResponse> getProgramInterests(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(profileService.getProgramInterests(profileId));
    }

    @GetMapping("/me/program-interests/search")
    public ResponseEntity<ProgramInterestSearchResponse> searchProgramInterests(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(profileService.searchProgramInterests(profileId, keyword));
    }

    @PutMapping("/me/program-interests")
    public ResponseEntity<ProgramInterestResponse> updateProgramInterests(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody UpdateProgramInterestsRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProgramInterests(profileId, request));
    }
}
