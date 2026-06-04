package com.example.kpick.profile.service;

import com.example.kpick.community.domain.CommunityComment;
import com.example.kpick.community.domain.CommunityPostType;
import com.example.kpick.community.repository.CommunityCommentRepository;
import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.community.uservote.domain.UserVote;
import com.example.kpick.community.uservote.repository.UserVoteRepository;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionAttender;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.mission.repository.MissionOptionRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.domain.ProfileBadge;
import com.example.kpick.profile.dto.req.CreateProfileBadgeRequest;
import com.example.kpick.profile.dto.req.UpdateNicknameRequest;
import com.example.kpick.profile.dto.req.UpdateProfileImageRequest;
import com.example.kpick.profile.dto.req.UpdateProgramInterestsRequest;
import com.example.kpick.profile.dto.res.CommunityActivityResponse;
import com.example.kpick.profile.dto.res.MissionHistoryResponse;
import com.example.kpick.profile.dto.res.MyPageResponse;
import com.example.kpick.profile.dto.res.NicknameCheckResponse;
import com.example.kpick.profile.dto.res.ProfileBadgeResponse;
import com.example.kpick.profile.dto.res.ProfileNicknameResponse;
import com.example.kpick.profile.dto.res.ProfileImageResponse;
import com.example.kpick.profile.dto.res.ProgramInterestResponse;
import com.example.kpick.profile.dto.res.ProgramInterestSearchResponse;
import com.example.kpick.profile.repository.ProfileBadgeRepository;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.notification.domain.PushNotificationType;
import com.example.kpick.notification.service.PushNotificationService;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.domain.ProgramInterest;
import com.example.kpick.program.repository.ProgramInterestRepository;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProgramRepository programRepository;
    private final ProgramInterestRepository programInterestRepository;
    private final MissionAttenderRepository missionAttenderRepository;
    private final MissionOptionRepository missionOptionRepository;
    private final ThreadRepository threadRepository;
    private final UserVoteRepository userVoteRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final ProfileBadgeRepository profileBadgeRepository;
    private final PushNotificationService pushNotificationService;

    private static final List<ProfileBadgeTemplate> DEFAULT_BADGES = List.of(
            new ProfileBadgeTemplate("FIRE_TORI", "불꽃토리", "3연속 정답", "🔥"),
            new ProfileBadgeTemplate("FIRST_PICK_TORI", "첫픽토리", "미션 첫 픽", "⚡"),
            new ProfileBadgeTemplate("RUNNING_TORI", "러닝토리", "시즌 1회 완주", "🏃"),
            new ProfileBadgeTemplate("ALL_PICK_TORI", "올픽토리", "전 미션 참여", "✅")
    );

    @Transactional
    public MyPageResponse getMyPage(Long profileId) {
        Profile profile = findProfile(profileId);
        assignInviteCodeIfMissing(profile);
        int seasonRank = findRank(profileId, Profile::getMissionPointValue);
        int totalRank = findRank(profileId, Profile::getTotalMissionPointValue);
        int communityRank = findRank(profileId, Profile::getActivityPointValue);

        return MyPageResponse.from(profile, seasonRank, totalRank, communityRank);
    }

    @Transactional(readOnly = true)
    public ProgramInterestResponse getProgramInterests(Long profileId) {
        findProfile(profileId);
        return ProgramInterestResponse.from(profileId, programInterestRepository.findByProfileId(profileId));
    }

    @Transactional(readOnly = true)
    public ProgramInterestSearchResponse searchProgramInterests(Long profileId, String keyword) {
        findProfile(profileId);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        Set<Long> interestedProgramIds = programInterestRepository.findByProfileId(profileId).stream()
                .map(interest -> interest.getProgram().getId())
                .collect(Collectors.toSet());
        List<Program> programs = normalizedKeyword.isBlank()
                ? List.of()
                : programRepository.findAll().stream()
                .filter(program -> program.getProgramName().toLowerCase().contains(normalizedKeyword.toLowerCase()))
                .toList();

        return ProgramInterestSearchResponse.from(profileId, normalizedKeyword, programs, interestedProgramIds);
    }

    @Transactional
    public ProgramInterestResponse updateProgramInterests(Long profileId, UpdateProgramInterestsRequest request) {
        validateUpdateProgramInterestsRequest(request);
        Profile profile = findProfile(profileId);
        Set<Long> programIds = new LinkedHashSet<>(request.getProgramIds());
        List<Program> programs = programIds.stream()
                .map(this::findProgram)
                .toList();

        programInterestRepository.deleteByProfileId(profileId);
        List<ProgramInterest> interests = programs.stream()
                .map(program -> ProgramInterest.builder()
                        .profile(profile)
                        .program(program)
                        .build())
                .toList();
        List<ProgramInterest> savedInterests = programInterestRepository.saveAll(interests);

        return ProgramInterestResponse.from(profileId, savedInterests);
    }

    @Transactional
    public ProfileNicknameResponse updateNickname(Long profileId, UpdateNicknameRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        Profile profile = findProfile(profileId);
        String nickname = normalizeRequired(request.getNickname(), "nickname");
        if (profileRepository.existsByNicknameAndIdNot(nickname, profileId)) {
            throw new IllegalArgumentException("Nickname already exists.");
        }
        profile.updateNickname(nickname);
        return ProfileNicknameResponse.from(profile);
    }

    @Transactional
    public ProfileImageResponse updateProfileImage(Long profileId, UpdateProfileImageRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        Profile profile = findProfile(profileId);
        profile.updateProfileImage(request.getProfileImageUrl());
        return ProfileImageResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(String nickname) {
        String normalizedNickname = normalizeRequired(nickname, "nickname");
        return new NicknameCheckResponse(normalizedNickname, !profileRepository.existsByNickname(normalizedNickname));
    }

    @Transactional(readOnly = true)
    public MissionHistoryResponse getMissionHistory(Long profileId, String resultFilter) {
        findProfile(profileId);
        String normalizedFilter = normalizeMissionHistoryFilter(resultFilter);
        List<MissionHistoryResponse.MissionHistoryItemResponse> items = missionAttenderRepository.findByProfileId(profileId).stream()
                .map(this::toMissionHistoryItem)
                .filter(item -> "ALL".equals(normalizedFilter) || item.getMissionResultStatus().equals(normalizedFilter))
                .toList();
        return new MissionHistoryResponse(profileId, normalizedFilter, items.size(), items);
    }

    @Transactional(readOnly = true)
    public CommunityActivityResponse getCommunityActivities(Long profileId, String activityType) {
        findProfile(profileId);
        String normalizedActivityType = normalizeCommunityActivityType(activityType);
        List<CommunityActivityResponse.CommunityActivityItemResponse> activities = new ArrayList<>();

        if ("POST".equals(normalizedActivityType) || "ALL".equals(normalizedActivityType)) {
            activities.addAll(threadRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                    .map(this::toThreadActivityItem)
                    .toList());
            activities.addAll(userVoteRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                    .map(this::toUserVoteActivityItem)
                    .toList());
        }
        if ("COMMENT".equals(normalizedActivityType) || "ALL".equals(normalizedActivityType)) {
            activities.addAll(communityCommentRepository.findByProfileIdOrderByCreatedAtDesc(profileId).stream()
                    .map(this::toCommentActivityItem)
                    .toList());
        }

        List<CommunityActivityResponse.CommunityActivityItemResponse> sortedActivities = activities.stream()
                .sorted(Comparator.comparing(CommunityActivityResponse.CommunityActivityItemResponse::getCreatedAt).reversed())
                .toList();
        return new CommunityActivityResponse(profileId, normalizedActivityType, sortedActivities.size(), sortedActivities);
    }

    @Transactional(readOnly = true)
    public ProfileBadgeResponse getBadges(Long profileId) {
        findProfile(profileId);
        List<ProfileBadge> acquiredProfileBadges = profileBadgeRepository.findByProfileId(profileId);
        Map<String, ProfileBadge> acquiredBadgeMap = acquiredProfileBadges.stream()
                .collect(Collectors.toMap(ProfileBadge::getBadgeCode, badge -> badge, (first, second) -> first));

        List<ProfileBadgeResponse.ProfileBadgeItemResponse> acquiredBadges = acquiredProfileBadges.stream()
                .map(ProfileBadgeResponse.ProfileBadgeItemResponse::acquired)
                .toList();
        List<ProfileBadgeResponse.ProfileBadgeItemResponse> lockedBadges = DEFAULT_BADGES.stream()
                .filter(template -> !acquiredBadgeMap.containsKey(template.badgeCode()))
                .map(template -> new ProfileBadgeResponse.ProfileBadgeItemResponse(
                        template.badgeCode(),
                        template.badgeName(),
                        template.description(),
                        template.emoji(),
                        false
                ))
                .toList();

        return new ProfileBadgeResponse(profileId, acquiredBadges.size(), acquiredBadges, lockedBadges);
    }

    @Transactional
    public ProfileBadgeResponse.ProfileBadgeItemResponse createBadge(Long profileId, CreateProfileBadgeRequest request) {
        findProfile(profileId);
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        String badgeCode = normalizeRequired(request.getBadgeCode(), "badgeCode");
        ProfileBadgeTemplate template = findBadgeTemplate(badgeCode);
        ProfileBadge badge = profileBadgeRepository.findByProfileIdAndBadgeCode(profileId, badgeCode)
                .orElseGet(() -> createBadgeAndNotify(profileId, request, template, badgeCode));
        return ProfileBadgeResponse.ProfileBadgeItemResponse.acquired(badge);
    }

    private ProfileBadge createBadgeAndNotify(
            Long profileId,
            CreateProfileBadgeRequest request,
            ProfileBadgeTemplate template,
            String badgeCode
    ) {
        ProfileBadge badge = profileBadgeRepository.save(ProfileBadge.create(
                profileId,
                badgeCode,
                valueOrDefault(request.getBadgeName(), template.badgeName()),
                valueOrDefault(request.getDescription(), template.description()),
                valueOrDefault(request.getEmoji(), template.emoji())
        ));
        pushNotificationService.notify(
                profileId,
                PushNotificationType.SPECIAL_BADGE,
                "스페셜 뱃지를 획득했어요",
                badge.getBadgeName() + " 뱃지를 확인해보세요.",
                "PROFILE_BADGE",
                badge.getId()
        );
        return badge;
    }

    private int findRank(Long profileId, ToLongFunction<Profile> scoreGetter) {
        List<Profile> sortedProfiles = profileRepository.findAll().stream()
                .sorted(Comparator.comparingLong(scoreGetter).reversed())
                .toList();
        return IntStream.range(0, sortedProfiles.size())
                .filter(index -> sortedProfiles.get(index).getId().equals(profileId))
                .map(index -> index + 1)
                .findFirst()
                .orElse(0);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private Program findProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found. programId=" + programId));
    }

    private MissionHistoryResponse.MissionHistoryItemResponse toMissionHistoryItem(MissionAttender attender) {
        Mission mission = attender.getMission();
        MissionOption selectedOption = attender.getMissionOption();
        MissionOption correctOption = findCorrectMissionOption(mission.getId());
        String resultStatus = getMissionResultStatus(mission, selectedOption, correctOption);
        long earnedPoint = "CORRECT".equals(resultStatus) ? calculateMissionRewardPoint(mission, correctOption) : 0L;
        Program program = programRepository.findById(mission.getProgramId()).orElse(null);

        return new MissionHistoryResponse.MissionHistoryItemResponse(
                mission.getId(),
                mission.getProgramId(),
                program == null ? null : program.getProgramName(),
                program == null ? null : program.getThumbnailImageUrl(),
                mission.getEpisode(),
                mission.getMissionName(),
                selectedOption.getId(),
                selectedOption.getContent(),
                correctOption == null ? null : correctOption.getId(),
                correctOption == null ? null : correctOption.getContent(),
                resultStatus,
                mission.getCoinFee(),
                earnedPoint,
                mission.getDueDateTime()
        );
    }

    private MissionOption findCorrectMissionOption(Long missionId) {
        return missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(missionId).stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .findFirst()
                .orElse(null);
    }

    private String getMissionResultStatus(Mission mission, MissionOption selectedOption, MissionOption correctOption) {
        if (mission.getMissionState() != MissionState.COMPLETED || correctOption == null) {
            return "PENDING";
        }
        return selectedOption.getId().equals(correctOption.getId()) ? "CORRECT" : "WRONG";
    }

    private long calculateMissionRewardPoint(Mission mission, MissionOption correctOption) {
        long correctCount = missionAttenderRepository.countByMissionIdAndMissionOptionId(mission.getId(), correctOption.getId());
        if (correctCount == 0) {
            return 0L;
        }
        long rewardPool = (long) mission.getCoinFee() * mission.getAttenderCount();
        return rewardPool / correctCount;
    }

    private CommunityActivityResponse.CommunityActivityItemResponse toThreadActivityItem(Thread thread) {
        return new CommunityActivityResponse.CommunityActivityItemResponse(
                "POST",
                CommunityPostType.THREAD,
                thread.getId(),
                null,
                thread.getTitle(),
                thread.getDescription(),
                thread.getLikeCount(),
                thread.getCommentCount(),
                thread.getCreatedAt()
        );
    }

    private CommunityActivityResponse.CommunityActivityItemResponse toUserVoteActivityItem(UserVote userVote) {
        return new CommunityActivityResponse.CommunityActivityItemResponse(
                "POST",
                CommunityPostType.USER_VOTE,
                userVote.getId(),
                null,
                userVote.getTitle(),
                userVote.getDescription(),
                userVote.getLikeCount(),
                userVote.getCommentCount(),
                userVote.getCreatedAt()
        );
    }

    private CommunityActivityResponse.CommunityActivityItemResponse toCommentActivityItem(CommunityComment comment) {
        return new CommunityActivityResponse.CommunityActivityItemResponse(
                "COMMENT",
                comment.getPostType(),
                comment.getPostId(),
                comment.getId(),
                findCommunityPostTitle(comment.getPostType(), comment.getPostId()),
                comment.getContent(),
                comment.getLikeCount(),
                0,
                comment.getCreatedAt()
        );
    }

    private String findCommunityPostTitle(CommunityPostType postType, Long postId) {
        if (postType == CommunityPostType.THREAD) {
            return threadRepository.findById(postId).map(Thread::getTitle).orElse(null);
        }
        if (postType == CommunityPostType.USER_VOTE) {
            return userVoteRepository.findById(postId).map(UserVote::getTitle).orElse(null);
        }
        return null;
    }

    private void validateUpdateProgramInterestsRequest(UpdateProgramInterestsRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProgramIds() == null) throw new IllegalArgumentException("programIds is required.");
        if (request.getProgramIds().stream().anyMatch(programId -> programId == null)) {
            throw new IllegalArgumentException("programId cannot be null.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private String normalizeMissionHistoryFilter(String resultFilter) {
        if (resultFilter == null || resultFilter.isBlank()) {
            return "ALL";
        }
        String normalizedFilter = resultFilter.trim().toUpperCase();
        if (!List.of("ALL", "CORRECT", "WRONG", "PENDING").contains(normalizedFilter)) {
            throw new IllegalArgumentException("resultFilter must be ALL, CORRECT, WRONG, or PENDING.");
        }
        return normalizedFilter;
    }

    private String normalizeCommunityActivityType(String activityType) {
        if (activityType == null || activityType.isBlank()) {
            return "POST";
        }
        String normalizedActivityType = activityType.trim().toUpperCase();
        if (!List.of("ALL", "POST", "COMMENT").contains(normalizedActivityType)) {
            throw new IllegalArgumentException("activityType must be ALL, POST, or COMMENT.");
        }
        return normalizedActivityType;
    }

    private ProfileBadgeTemplate findBadgeTemplate(String badgeCode) {
        return DEFAULT_BADGES.stream()
                .filter(template -> template.badgeCode().equals(badgeCode))
                .findFirst()
                .orElse(new ProfileBadgeTemplate(badgeCode, badgeCode, "임시 활동 뱃지", null));
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private void assignInviteCodeIfMissing(Profile profile) {
        if (profile.getInviteCode() != null && !profile.getInviteCode().isBlank()) {
            return;
        }
        profile.assignInviteCode(generateUniqueInviteCode());
    }

    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = randomLetters(5) + "-" + randomLetters(2) + randomDigits(2);
        } while (profileRepository.existsByInviteCode(inviteCode));
        return inviteCode;
    }

    private String randomLetters(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) ('A' + ThreadLocalRandom.current().nextInt(26)));
        }
        return builder.toString();
    }

    private String randomDigits(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }

    private static class ProfileBadgeTemplate {
        private final String badgeCode;
        private final String badgeName;
        private final String description;
        private final String emoji;

        private ProfileBadgeTemplate(String badgeCode, String badgeName, String description, String emoji) {
            this.badgeCode = badgeCode;
            this.badgeName = badgeName;
            this.description = description;
            this.emoji = emoji;
        }

        private String badgeCode() {
            return badgeCode;
        }

        private String badgeName() {
            return badgeName;
        }

        private String description() {
            return description;
        }

        private String emoji() {
            return emoji;
        }
    }
}
