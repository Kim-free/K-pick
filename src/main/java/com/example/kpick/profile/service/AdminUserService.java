package com.example.kpick.profile.service;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.repository.AppUserRepository;
import com.example.kpick.benefit.dto.res.PickHistoryResponse;
import com.example.kpick.benefit.repository.PickHistoryRepository;
import com.example.kpick.profile.domain.AdminUserStatus;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.dto.res.AdminUserDetailsResponse;
import com.example.kpick.profile.dto.res.AdminUserListResponse;
import com.example.kpick.profile.dto.res.CommunityActivityResponse;
import com.example.kpick.profile.dto.res.MissionHistoryResponse;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.ranking.domain.PointTier;
import com.example.kpick.report.domain.UserSanction;
import com.example.kpick.report.domain.UserSanctionType;
import com.example.kpick.report.repository.UserSanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final ProfileRepository profileRepository;
    private final AppUserRepository appUserRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final PickHistoryRepository pickHistoryRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public List<AdminUserListResponse> getUsers(String keyword, AdminUserStatus userStatus) {
        return profileRepository.findAll().stream()
                .map(this::toListResponse)
                .flatMap(Optional::stream)
                .filter(user -> matchesKeyword(user, keyword))
                .filter(user -> userStatus == null || user.getUserStatus() == userStatus)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserDetailsResponse getUserDetails(Long profileId) {
        Profile profile = findProfile(profileId);
        AppUser appUser = findAppUser(profile.getAppUserId());
        UserSanction currentSanction = findCurrentSanction(profileId);
        MissionHistoryResponse missionHistory = profileService.getMissionHistory(profileId, "ALL");
        CommunityActivityResponse communityActivity = profileService.getCommunityActivities(profileId, "ALL");
        List<PickHistoryResponse.PickHistoryItemResponse> pickHistories = pickHistoryRepository
                .findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                .map(PickHistoryResponse.PickHistoryItemResponse::from)
                .toList();

        return new AdminUserDetailsResponse(
                profile.getId(),
                profile.getNickname(),
                appUser.getEmail(),
                appUser.getLoginType(),
                PointTier.from(profile.getTotalMissionPointValue()).getDisplayName(profile.getTotalMissionPointValue()),
                profile.getCoin() == null ? 0L : profile.getCoin(),
                profile.getTotalMissionPointValue(),
                calculateCorrectRate(missionHistory.getMissions()),
                resolveUserStatus(currentSanction),
                currentSanction == null ? null : currentSanction.getSanctionType(),
                currentSanction == null ? null : currentSanction.getEndsAt(),
                appUser.getCreatedAt(),
                appUser.getLastLoginAt(),
                missionHistory.getMissions(),
                communityActivity.getActivities(),
                pickHistories
        );
    }

    private Optional<AdminUserListResponse> toListResponse(Profile profile) {
        return appUserRepository.findById(profile.getAppUserId())
                .map(appUser -> new AdminUserListResponse(
                        profile.getId(),
                        profile.getNickname(),
                        appUser.getEmail(),
                        PointTier.from(profile.getTotalMissionPointValue()).getDisplayName(profile.getTotalMissionPointValue()),
                        profile.getCoin() == null ? 0L : profile.getCoin(),
                        appUser.getCreatedAt(),
                        resolveUserStatus(findCurrentSanction(profile.getId()))
                ));
    }

    private int calculateCorrectRate(List<MissionHistoryResponse.MissionHistoryItemResponse> missions) {
        long completedCount = missions.stream()
                .filter(mission -> !"PENDING".equals(mission.getMissionResultStatus()))
                .count();
        if (completedCount == 0) {
            return 0;
        }
        long correctCount = missions.stream()
                .filter(mission -> "CORRECT".equals(mission.getMissionResultStatus()))
                .count();
        return (int) Math.round(correctCount * 100.0 / completedCount);
    }

    private boolean matchesKeyword(AdminUserListResponse user, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        return containsIgnoreCase(user.getNickname(), normalizedKeyword)
                || containsIgnoreCase(user.getEmail(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase().contains(normalizedKeyword);
    }

    private AdminUserStatus resolveUserStatus(UserSanction sanction) {
        if (sanction == null || sanction.getSanctionType() == UserSanctionType.WARNING) {
            return AdminUserStatus.NORMAL;
        }
        return sanction.getSanctionType() == UserSanctionType.PERMANENT
                ? AdminUserStatus.PERMANENTLY_SUSPENDED
                : AdminUserStatus.SUSPENDED;
    }

    private UserSanction findCurrentSanction(Long profileId) {
        LocalDateTime now = LocalDateTime.now();
        return userSanctionRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                .filter(sanction -> sanction.getSanctionType() == UserSanctionType.PERMANENT
                        || sanction.getEndsAt() != null && sanction.getEndsAt().isAfter(now))
                .findFirst()
                .orElse(null);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private AppUser findAppUser(Long appUserId) {
        return appUserRepository.findById(appUserId)
                .orElseThrow(() -> new IllegalArgumentException("App user not found. appUserId=" + appUserId));
    }
}
