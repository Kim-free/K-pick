package com.example.kpick.mission.service;

import com.example.kpick.community.thread.domain.Thread;
import com.example.kpick.community.thread.repository.ThreadRepository;
import com.example.kpick.benefit.domain.PickHistory;
import com.example.kpick.benefit.domain.PickHistoryType;
import com.example.kpick.benefit.repository.PickHistoryRepository;
import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionAttender;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.dto.req.SelectMissionOptionRequest;
import com.example.kpick.mission.dto.res.MissionConfirmModalResponse;
import com.example.kpick.mission.dto.res.MissionDetailsResponse;
import com.example.kpick.mission.dto.res.MissionListResponse;
import com.example.kpick.mission.dto.res.MissionRelatedContentResponse;
import com.example.kpick.mission.dto.res.MissionRelatedContentResponse.RelatedMissionResponse;
import com.example.kpick.mission.dto.res.MissionRelatedContentResponse.RelatedThreadResponse;
import com.example.kpick.mission.dto.res.MissionRecommendationResponse;
import com.example.kpick.mission.dto.res.MissionResultResponse;
import com.example.kpick.mission.dto.res.MissionResultResponse.OptionResultResponse;
import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.mission.repository.MissionOptionRepository;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.domain.ProgramInterest;
import com.example.kpick.program.repository.ProgramInterestRepository;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionQueryService {
    private static final Pattern EPISODE_PATTERN = Pattern.compile("(\\d+\\s*(화|회|기|편))");

    private final MissionRepository missionRepository;
    private final MissionOptionRepository missionOptionRepository;
    private final MissionAttenderRepository missionAttenderRepository;
    private final ThreadRepository threadRepository;
    private final ProgramRepository programRepository;
    private final ProgramInterestRepository programInterestRepository;
    private final ProfileRepository profileRepository;
    private final PickHistoryRepository pickHistoryRepository;

    @Transactional(readOnly = true)
    public List<MissionListResponse> getMissionsByStatus(MissionState missionState) {
        List<Mission> missions = missionRepository.findByMissionStateOrderByDueDateTimeAsc(missionState);
        Map<Long, Program> programMap = programRepository.findAllById(
                        missions.stream()
                                .map(Mission::getProgramId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        return missions.stream()
                .map(mission -> MissionListResponse.from(mission, programMap.get(mission.getProgramId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MissionRecommendationResponse> getMissionRecommendations(Long profileId, Long programId) {
        List<Mission> missions = findRecommendationMissions(profileId, programId);
        if (missions.isEmpty()) {
            return List.of();
        }

        Map<Long, Program> programMap = programRepository.findAllById(
                        missions.stream()
                                .map(Mission::getProgramId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        return missions.stream()
                .map(mission -> MissionRecommendationResponse.from(
                        mission,
                        programMap.get(mission.getProgramId()),
                        missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(mission.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionRelatedContentResponse getRelatedContents(Long missionId) {
        Mission baseMission = findMission(missionId);
        Program baseProgram = findProgram(baseMission.getProgramId());
        Map<Long, Program> programMap = programRepository.findAll().stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        RelatedMissionResponse relatedMission = findRelatedMission(baseMission, baseProgram, programMap);
        RelatedThreadResponse relatedThread = findRelatedThread(baseMission, baseProgram, programMap);

        return MissionRelatedContentResponse.from(relatedMission, relatedThread);
    }

    @Transactional(readOnly = true)
    public Object getMissionDetails(Long missionId, Long profileId) {
        Mission mission = findMission(missionId);
        if (mission.getMissionState() == MissionState.COMPLETED) {
            return buildMissionResultResponse(mission, profileId);
        }

        Program program = findProgram(mission.getProgramId());
        Long profileCoin = profileId == null ? null : findProfile(profileId).getCoin();
        List<MissionOption> options = missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(missionId);

        return MissionDetailsResponse.from(mission, program, profileCoin, options);
    }

    @Transactional
    public MissionConfirmModalResponse selectMissionOption(Long missionId, SelectMissionOptionRequest request) {
        validateSelectMissionOptionRequest(request);

        MissionAttender missionAttender = createMissionAttender(missionId, request.getProfileId(), request.getMissionOptionId());
        return MissionConfirmModalResponse.from(missionAttender.getMission(), missionAttender, missionAttender.getProfile().getCoin());
    }

    @Transactional(readOnly = true)
    public MissionConfirmModalResponse getConfirmModal(Long missionId, Long profileId) {
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required.");
        }

        Mission mission = findMission(missionId);
        Profile profile = findProfile(profileId);
        MissionAttender missionAttender = missionAttenderRepository.findByMissionIdAndProfileId(missionId, profileId)
                .orElseThrow(() -> new IllegalArgumentException("Mission attender not found. missionId=" + missionId + ", profileId=" + profileId));

        return MissionConfirmModalResponse.from(mission, missionAttender, profile.getCoin());
    }

    private MissionResultResponse buildMissionResultResponse(Mission mission, Long profileId) {
        Program program = findProgram(mission.getProgramId());
        Long missionId = mission.getId();
        List<MissionOption> options = missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(missionId);
        MissionOption correctOption = options.stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .findFirst()
                .orElse(null);
        MissionAttender missionAttender = profileId == null
                ? null
                : missionAttenderRepository.findByMissionIdAndProfileId(missionId, profileId).orElse(null);
        long totalAttenderCount = missionAttenderRepository.findByMissionId(missionId).size();
        List<OptionResultResponse> optionResults = options.stream()
                .map(option -> OptionResultResponse.from(
                        option,
                        missionAttenderRepository.countByMissionIdAndMissionOptionId(missionId, option.getId()),
                        totalAttenderCount
                ))
                .toList();
        Integer earnedPoint = calculateEarnedPoint(mission, correctOption, missionAttender);

        return MissionResultResponse.from(mission, program, correctOption, missionAttender, earnedPoint, optionResults);
    }

    private MissionAttender createMissionAttender(Long missionId, Long profileId, Long missionOptionId) {
        Mission mission = findMission(missionId);
        validateAttendableMission(mission);
        Profile profile = findProfile(profileId);
        MissionOption missionOption = missionOptionRepository.findById(missionOptionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission option not found. missionOptionId=" + missionOptionId));

        if (!missionOption.getMission().getId().equals(missionId)) {
            throw new IllegalArgumentException("Mission option does not belong to this mission.");
        }
        if (missionAttenderRepository.existsByMissionIdAndProfileId(missionId, profileId)) {
            throw new IllegalArgumentException("Already attended mission.");
        }

        profile.deductCoin(mission.getCoinFee());
        pickHistoryRepository.save(PickHistory.create(
                profileId,
                profile.getNickname(),
                PickHistoryType.MISSION_PARTICIPATION,
                -mission.getCoinFee(),
                "미션 참여"
        ));
        mission.increaseAttenderCount();

        MissionAttender missionAttender = MissionAttender.builder()
                .mission(mission)
                .profile(profile)
                .missionOption(missionOption)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        return missionAttenderRepository.save(missionAttender);
    }

    private void validateAttendableMission(Mission mission) {
        if (Boolean.FALSE.equals(mission.getIsActive())) {
            throw new IllegalArgumentException("Inactive mission cannot be attended.");
        }
        if (mission.getMissionState() == MissionState.COMPLETED) {
            throw new IllegalArgumentException("Completed mission cannot be attended.");
        }
        if (mission.getDueDateTime() != null && !mission.getDueDateTime().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Closed mission cannot be attended.");
        }
    }

    private Mission findMission(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found. missionId=" + missionId));
    }

    private Program findProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found. programId=" + programId));
    }

    private RelatedMissionResponse findRelatedMission(Mission baseMission, Program baseProgram, Map<Long, Program> programMap) {
        return missionRepository.findAllByOrderByIdDesc().stream()
                .filter(mission -> !mission.getId().equals(baseMission.getId()))
                .filter(mission -> getRelatedPriority(baseMission, baseProgram, mission, programMap.get(mission.getProgramId())) <= 3)
                .sorted((left, right) -> Integer.compare(
                        getRelatedPriority(baseMission, baseProgram, left, programMap.get(left.getProgramId())),
                        getRelatedPriority(baseMission, baseProgram, right, programMap.get(right.getProgramId()))
                ))
                .findFirst()
                .map(mission -> RelatedMissionResponse.from(mission, programMap.get(mission.getProgramId())))
                .orElse(null);
    }

    private RelatedThreadResponse findRelatedThread(Mission baseMission, Program baseProgram, Map<Long, Program> programMap) {
        return threadRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(thread -> getRelatedThreadPriority(baseMission, baseProgram, thread, programMap) <= 3)
                .sorted((left, right) -> Integer.compare(
                        getRelatedThreadPriority(baseMission, baseProgram, left, programMap),
                        getRelatedThreadPriority(baseMission, baseProgram, right, programMap)
                ))
                .findFirst()
                .map(thread -> RelatedThreadResponse.from(thread, programMap.get(thread.getProgramId())))
                .orElse(null);
    }

    private int getRelatedThreadPriority(Mission baseMission, Program baseProgram, Thread thread, Map<Long, Program> programMap) {
        Mission sharedMission = thread.getMissionId() == null
                ? null
                : missionRepository.findById(thread.getMissionId()).orElse(null);
        if (sharedMission != null && hasSameProgramAndEpisode(baseMission, sharedMission)) {
            return 1;
        }
        Program threadProgram = programMap.get(thread.getProgramId());
        if (threadProgram == null) {
            return Integer.MAX_VALUE;
        }
        if (thread.getProgramId().equals(baseMission.getProgramId())) {
            return 2;
        }
        if (threadProgram.getGenre() == baseProgram.getGenre()) {
            return 3;
        }
        return Integer.MAX_VALUE;
    }

    private int getRelatedPriority(Mission baseMission, Program baseProgram, Mission candidate, Program candidateProgram) {
        if (candidateProgram == null) {
            return Integer.MAX_VALUE;
        }
        if (hasSameProgramAndEpisode(baseMission, candidate)) {
            return 1;
        }
        if (candidate.getProgramId().equals(baseMission.getProgramId())) {
            return 2;
        }
        if (candidateProgram.getGenre() == baseProgram.getGenre()) {
            return 3;
        }
        return Integer.MAX_VALUE;
    }

    private boolean hasSameProgramAndEpisode(Mission baseMission, Mission candidate) {
        if (!candidate.getProgramId().equals(baseMission.getProgramId())) {
            return false;
        }
        String baseEpisode = extractEpisode(baseMission.getMissionName());
        String candidateEpisode = extractEpisode(candidate.getMissionName());
        return baseEpisode != null && baseEpisode.equals(candidateEpisode);
    }

    private String extractEpisode(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = EPISODE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replaceAll("\\s+", "");
    }

    private List<Mission> findRecommendationMissions(Long profileId, Long programId) {
        if (programId != null) {
            return missionRepository.findByMissionStateAndProgramIdOrderByIdDesc(MissionState.ONGOING, programId);
        }
        if (profileId == null) {
            throw new IllegalArgumentException("profileId is required when programId is not provided.");
        }

        List<Long> interestedProgramIds = programInterestRepository.findByProfileId(profileId).stream()
                .map(ProgramInterest::getProgram)
                .map(Program::getId)
                .toList();
        if (interestedProgramIds.isEmpty()) {
            return List.of();
        }

        return missionRepository.findByMissionStateAndProgramIdInOrderByIdDesc(MissionState.ONGOING, interestedProgramIds);
    }

    private Profile findProfile(Long profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found. profileId=" + profileId));
    }

    private void validateSelectMissionOptionRequest(SelectMissionOptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required.");
        }
        if (request.getMissionOptionId() == null) {
            throw new IllegalArgumentException("missionOptionId is required.");
        }
    }

    private Integer calculateEarnedPoint(Mission mission, MissionOption correctOption, MissionAttender missionAttender) {
        if (correctOption == null || missionAttender == null) {
            return null;
        }
        if (!missionAttender.getMissionOption().getId().equals(correctOption.getId())) {
            return 0;
        }

        long correctAttenderCount = missionAttenderRepository.countByMissionIdAndMissionOptionId(mission.getId(), correctOption.getId());
        if (correctAttenderCount == 0) {
            return 0;
        }

        return (int) Math.floor((mission.getCoinFee() * (long) mission.getAttenderCount()) / (double) correctAttenderCount);
    }
}
