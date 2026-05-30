package com.example.kpick.program.service;

import com.example.kpick.mission.domain.Mission;
import com.example.kpick.mission.repository.MissionRepository;
import com.example.kpick.program.domain.Program;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.dto.req.CreateProgramRequest;
import com.example.kpick.program.dto.req.UpdateProgramRequest;
import com.example.kpick.program.dto.res.ProgramDetailsResponse;
import com.example.kpick.program.dto.res.ProgramEpisodeResponse;
import com.example.kpick.program.dto.res.ProgramListResponse;
import com.example.kpick.program.dto.res.ProgramSelectionResponse;
import com.example.kpick.program.dto.res.ProgramResponse;
import com.example.kpick.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final MissionRepository missionRepository;

    @Transactional
    public ProgramResponse createProgram(CreateProgramRequest request) {
        validateCreateProgramRequest(request);

        Program program = Program.toEntity(request);
        Program savedProgram = programRepository.save(program);

        return ProgramResponse.from(savedProgram);
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getPrograms(Boolean isOnAir) {
        return getPrograms(null, null, isOnAir, null);
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getPrograms(String keyword, Genre genre, Boolean isOnAir, Boolean isExposed) {
        return programRepository.findAll().stream()
                .filter(program -> keyword == null || keyword.isBlank()
                        || program.getProgramName().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(program -> genre == null || program.getGenre() == genre)
                .filter(program -> isOnAir == null || program.isOnAir() == isOnAir)
                .filter(program -> isExposed == null || program.isExposed() == isExposed)
                .map(ProgramResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramListResponse> getRegisteredPrograms() {
        return programRepository.findAll().stream()
                .map(ProgramListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramSelectionResponse> getProgramSelections(String keyword) {
        return programRepository.findAll().stream()
                .filter(program -> keyword == null || keyword.isBlank()
                        || program.getProgramName().toLowerCase().contains(keyword.trim().toLowerCase()))
                .map(program -> ProgramSelectionResponse.from(program, createEpisodeLabels(program.getEpisodeCount())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramEpisodeResponse getProgramEpisodes(Long programId) {
        Program program = findProgram(programId);
        return ProgramEpisodeResponse.from(program, createEpisodeLabels(program.getEpisodeCount()));
    }

    @Transactional(readOnly = true)
    public ProgramDetailsResponse getProgramDetails(Long programId) {
        Program program = findProgram(programId);
        List<Mission> missions = missionRepository.findByProgramIdOrderByDueDateTimeDesc(programId);

        return ProgramDetailsResponse.from(program, missions);
    }

    @Transactional
    public ProgramResponse updateProgram(Long programId, UpdateProgramRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        validateUpdateProgramRequest(request);

        Program program = findProgram(programId);
        program.update(request);

        return ProgramResponse.from(program);
    }

    @Transactional
    public void deleteProgram(Long programId) {
        Program program = findProgram(programId);
        programRepository.delete(program);
    }

    private Program findProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found. programId=" + programId));
    }

    private void validateCreateProgramRequest(CreateProgramRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getProgramName() == null || request.getProgramName().isBlank()) {
            throw new IllegalArgumentException("programName is required.");
        }
        if (request.getBroadcaster() == null || request.getBroadcaster().isBlank()) {
            throw new IllegalArgumentException("broadcaster is required.");
        }
        if (request.getGenre() == null) {
            throw new IllegalArgumentException("genre is required.");
        }
        if (request.getEpisodeCount() == null) {
            throw new IllegalArgumentException("episodeCount is required.");
        }
        if (request.getEpisodeCount() < 0) {
            throw new IllegalArgumentException("episodeCount cannot be negative.");
        }
        if (request.getBroadcastStartDate() != null && request.getBroadcastEndDate() != null
                && request.getBroadcastEndDate().isBefore(request.getBroadcastStartDate())) {
            throw new IllegalArgumentException("broadcastEndDate cannot be before broadcastStartDate.");
        }
        if (request.getIsExposed() == null) {
            throw new IllegalArgumentException("isExposed is required.");
        }
    }

    private void validateUpdateProgramRequest(UpdateProgramRequest request) {
        if (request.getProgramName() != null && request.getProgramName().isBlank()) {
            throw new IllegalArgumentException("programName cannot be blank.");
        }
        if (request.getBroadcaster() != null && request.getBroadcaster().isBlank()) {
            throw new IllegalArgumentException("broadcaster cannot be blank.");
        }
        if (request.getSeason() != null && request.getSeason().isBlank()) {
            throw new IllegalArgumentException("season cannot be blank.");
        }
        if (request.getDescription() != null && request.getDescription().isBlank()) {
            throw new IllegalArgumentException("description cannot be blank.");
        }
        if (request.getEpisodeCount() != null && request.getEpisodeCount() < 0) {
            throw new IllegalArgumentException("episodeCount cannot be negative.");
        }
        if (request.getBroadcastStartDate() != null && request.getBroadcastEndDate() != null
                && request.getBroadcastEndDate().isBefore(request.getBroadcastStartDate())) {
            throw new IllegalArgumentException("broadcastEndDate cannot be before broadcastStartDate.");
        }
    }

    private List<String> createEpisodeLabels(int episodeCount) {
        return IntStream.iterate(episodeCount, episode -> episode - 1)
                .limit(episodeCount)
                .mapToObj(episode -> episode + "화")
                .toList();
    }
}
