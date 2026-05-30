package com.example.kpick.mission.service;

import com.example.kpick.mission.domain.MissionSuggestion;
import com.example.kpick.mission.domain.MissionSuggestionOption;
import com.example.kpick.mission.dto.req.CreateMissionSuggestionRequest;
import com.example.kpick.mission.dto.res.MissionSuggestionResponse;
import com.example.kpick.mission.repository.MissionSuggestionOptionRepository;
import com.example.kpick.mission.repository.MissionSuggestionRepository;
import com.example.kpick.profile.repository.ProfileRepository;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class MissionSuggestionService {
    private final MissionSuggestionRepository missionSuggestionRepository;
    private final MissionSuggestionOptionRepository missionSuggestionOptionRepository;
    private final ProfileRepository profileRepository;
    private final ProgramRepository programRepository;

    @Transactional
    public MissionSuggestionResponse createMissionSuggestion(CreateMissionSuggestionRequest request) {
        validateCreateMissionSuggestionRequest(request);
        validateProfileExists(request.getProfileId());
        validateProgramExists(request.getProgramId());

        MissionSuggestion suggestion = missionSuggestionRepository.save(MissionSuggestion.toEntity(request));
        saveOptions(suggestion, request.getOptions());

        return MissionSuggestionResponse.from(suggestion);
    }

    private List<MissionSuggestionOption> saveOptions(
            MissionSuggestion suggestion,
            List<CreateMissionSuggestionRequest.MissionSuggestionOptionRequest> optionRequests
    ) {
        List<MissionSuggestionOption> options = IntStream.range(0, optionRequests.size())
                .mapToObj(index -> MissionSuggestionOption.builder()
                        .missionSuggestion(suggestion)
                        .content(optionRequests.get(index).getContent().trim())
                        .displayOrder(index + 1)
                        .build())
                .toList();
        return missionSuggestionOptionRepository.saveAll(options);
    }

    private void validateCreateMissionSuggestionRequest(CreateMissionSuggestionRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getProfileId() == null) throw new IllegalArgumentException("profileId is required.");
        if (request.getProgramId() == null) throw new IllegalArgumentException("programId is required.");
        if (request.getGenre() == null) throw new IllegalArgumentException("genre is required.");
        if (request.getMissionTitle() == null || request.getMissionTitle().isBlank()) throw new IllegalArgumentException("missionTitle is required.");
        if (request.getOptions() == null || request.getOptions().size() < 2) throw new IllegalArgumentException("At least two options are required.");
        if (request.getOptions().stream().anyMatch(option -> option == null || option.getContent() == null || option.getContent().isBlank())) {
            throw new IllegalArgumentException("Option content is required.");
        }
    }

    private void validateProfileExists(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Profile not found. profileId=" + profileId);
        }
    }

    private void validateProgramExists(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new IllegalArgumentException("Program not found. programId=" + programId);
        }
    }
}
