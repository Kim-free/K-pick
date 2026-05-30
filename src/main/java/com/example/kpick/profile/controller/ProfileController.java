package com.example.kpick.profile.controller;

import com.example.kpick.profile.dto.req.CreateProfileBadgeRequest;
import com.example.kpick.profile.dto.req.UpdateNicknameRequest;
import com.example.kpick.profile.dto.req.UpdateProgramInterestsRequest;
import com.example.kpick.profile.dto.res.CommunityActivityResponse;
import com.example.kpick.profile.dto.res.MissionHistoryResponse;
import com.example.kpick.profile.dto.res.MyPageResponse;
import com.example.kpick.profile.dto.res.NicknameCheckResponse;
import com.example.kpick.profile.dto.res.ProfileBadgeResponse;
import com.example.kpick.profile.dto.res.ProfileNicknameResponse;
import com.example.kpick.profile.dto.res.ProgramInterestResponse;
import com.example.kpick.profile.dto.res.ProgramInterestSearchResponse;
import com.example.kpick.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/{profileId}/my-page")
    public ResponseEntity<MyPageResponse> getMyPage(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getMyPage(profileId));
    }

    @PatchMapping("/{profileId}/nickname")
    public ResponseEntity<ProfileNicknameResponse> updateNickname(
            @PathVariable Long profileId,
            @RequestBody UpdateNicknameRequest request
    ) {
        return ResponseEntity.ok(profileService.updateNickname(profileId, request));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(profileService.checkNickname(nickname));
    }

    @GetMapping("/{profileId}/mission-history")
    public ResponseEntity<MissionHistoryResponse> getMissionHistory(
            @PathVariable Long profileId,
            @RequestParam(required = false) String resultFilter
    ) {
        return ResponseEntity.ok(profileService.getMissionHistory(profileId, resultFilter));
    }

    @GetMapping("/{profileId}/community-activities")
    public ResponseEntity<CommunityActivityResponse> getCommunityActivities(
            @PathVariable Long profileId,
            @RequestParam(required = false) String activityType
    ) {
        return ResponseEntity.ok(profileService.getCommunityActivities(profileId, activityType));
    }

    @GetMapping("/{profileId}/badges")
    public ResponseEntity<ProfileBadgeResponse> getBadges(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getBadges(profileId));
    }

    @PostMapping("/{profileId}/badges")
    public ResponseEntity<ProfileBadgeResponse.ProfileBadgeItemResponse> createBadge(
            @PathVariable Long profileId,
            @RequestBody CreateProfileBadgeRequest request
    ) {
        return ResponseEntity.ok(profileService.createBadge(profileId, request));
    }

    @GetMapping("/{profileId}/program-interests")
    public ResponseEntity<ProgramInterestResponse> getProgramInterests(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getProgramInterests(profileId));
    }

    @GetMapping("/{profileId}/program-interests/search")
    public ResponseEntity<ProgramInterestSearchResponse> searchProgramInterests(
            @PathVariable Long profileId,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(profileService.searchProgramInterests(profileId, keyword));
    }

    @PutMapping("/{profileId}/program-interests")
    public ResponseEntity<ProgramInterestResponse> updateProgramInterests(
            @PathVariable Long profileId,
            @RequestBody UpdateProgramInterestsRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProgramInterests(profileId, request));
    }
}
