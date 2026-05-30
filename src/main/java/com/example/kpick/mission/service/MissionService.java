package com.example.kpick.mission.service;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.domain.MissionAttender;
import com.example.kpick.mission.domain.MissionOption;
import com.example.kpick.mission.domain.MissionState;
import com.example.kpick.mission.dto.req.ConfirmMissionResultRequest;
import com.example.kpick.mission.dto.req.CreateMissionRequest;
import com.example.kpick.mission.dto.req.CreateMissionRequest.MissionOptionRequest;
import com.example.kpick.mission.dto.res.MissionAdminResponse;
import com.example.kpick.mission.dto.res.MissionResponse;
import com.example.kpick.mission.repository.MissionAttenderRepository;
import com.example.kpick.mission.repository.MissionOptionRepository;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionOptionRepository missionOptionRepository;
    private final MissionAttenderRepository missionAttenderRepository;
    private final ProgramRepository programRepository;

    @Transactional(readOnly = true)
    public List<MissionAdminResponse> getAdminMissions(String keyword, Long programId, MissionState missionState) {
        Map<Long, Program> programMap = programRepository.findAll().stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        return missionRepository.findAllByOrderByIdDesc().stream()
                .filter(mission -> programId == null || mission.getProgramId().equals(programId))
                .filter(mission -> missionState == null || mission.getMissionState() == missionState)
                .filter(mission -> matchesKeyword(mission, programMap.get(mission.getProgramId()), keyword))
                .map(mission -> MissionAdminResponse.from(
                        mission,
                        programMap.get(mission.getProgramId()),
                        missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(mission.getId()),
                        getOptionSelectionCounts(mission.getId())
                ))
                .toList();
    }

    @Transactional
    public MissionResponse createMission(CreateMissionRequest request) {
        validateCreateMissionRequest(request);

        Mission mission = Mission.toEntity(request);

        Mission savedMission = missionRepository.save(mission);
        List<MissionOption> options = saveOptions(savedMission, request.getOptions());

        return MissionResponse.from(savedMission, options);
    }

    @Transactional
    public MissionResponse confirmResult(Long missionId, ConfirmMissionResultRequest request) {
        if (request == null || request.getCorrectMissionOptionId() == null) {
            throw new IllegalArgumentException("correctMissionOptionId is required.");
        }

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found. missionId=" + missionId));
        if (mission.getMissionState() == MissionState.COMPLETED) {
            throw new IllegalArgumentException("Mission result is already confirmed.");
        }
        List<MissionOption> options = missionOptionRepository.findByMissionIdOrderByDisplayOrderAsc(missionId);

        boolean hasCorrectOption = options.stream()
                .anyMatch(option -> option.getId().equals(request.getCorrectMissionOptionId()));
        if (!hasCorrectOption) {
            throw new IllegalArgumentException("Correct option does not belong to this mission.");
        }

        options.forEach(option -> option.markCorrect(option.getId().equals(request.getCorrectMissionOptionId())));
        mission.complete(request.getResultPublishTiming());
        rewardCorrectAttenders(mission, request.getCorrectMissionOptionId());

        return MissionResponse.from(mission, options);
    }

    private void rewardCorrectAttenders(Mission mission, Long correctMissionOptionId) {
        List<MissionAttender> correctAttenders = missionAttenderRepository.findByMissionIdAndMissionOptionId(mission.getId(), correctMissionOptionId);
        if (correctAttenders.isEmpty()) {
            return;
        }

        long totalRewardPool = mission.getCoinFee() * (long) mission.getAttenderCount();
        long rewardPoint = (long) Math.floor(totalRewardPool / (double) correctAttenders.size());
        correctAttenders.forEach(attender -> {
            Profile profile = attender.getProfile();
            profile.addMissionPoint(rewardPoint);
        });
    }

    private List<MissionOption> saveOptions(Mission mission, List<MissionOptionRequest> optionRequests) {
        List<MissionOption> options = IntStream.range(0, optionRequests.size())
                .mapToObj(index -> {
                    MissionOptionRequest option = optionRequests.get(index);
                    return MissionOption.builder()
                        .mission(mission)
                        .content(option.getContent().trim())
                        .displayOrder(index + 1)
                        .isCorrect(false)
                        .build();
                })
                .toList();

        return missionOptionRepository.saveAll(options);
    }

    private Map<Long, Long> getOptionSelectionCounts(Long missionId) {
        return missionAttenderRepository.findByMissionId(missionId).stream()
                .collect(Collectors.groupingBy(attender -> attender.getMissionOption().getId(), Collectors.counting()));
    }

    private boolean matchesKeyword(Mission mission, Program program, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return mission.getMissionName().toLowerCase().contains(normalizedKeyword)
                || (program != null && program.getProgramName().toLowerCase().contains(normalizedKeyword));
    }

    private void validateCreateMissionRequest(CreateMissionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getProgramId() == null) {
            throw new IllegalArgumentException("programId is required.");
        }
        if (request.getProfileId() == null) {
            throw new IllegalArgumentException("profileId is required.");
        }
        if (request.getMissionName() == null || request.getMissionName().isBlank()) {
            throw new IllegalArgumentException("missionName is required.");
        }
        if (request.getEpisode() == null || request.getEpisode().isBlank()) {
            throw new IllegalArgumentException("episode is required.");
        }
        if (request.getDueDateTime() == null) {
            throw new IllegalArgumentException("dueDateTime is required.");
        }
        if (request.getCoinFee() == null || request.getCoinFee() < 0) {
            throw new IllegalArgumentException("coinFee must be zero or positive.");
        }
        if (request.getOptions() == null || request.getOptions().size() < 2) {
            throw new IllegalArgumentException("At least two mission options are required.");
        }
        if (request.getOptions().stream().anyMatch(option -> option == null || option.getContent() == null || option.getContent().isBlank())) {
            throw new IllegalArgumentException("Mission option content is required.");
        }
    }
}
