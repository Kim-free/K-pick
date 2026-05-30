package com.example.kpick.program.controller;

import com.example.kpick.program.dto.res.ProgramEpisodeResponse;
import com.example.kpick.program.dto.res.ProgramListResponse;
import com.example.kpick.program.dto.res.ProgramSelectionResponse;
import com.example.kpick.program.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProgramController {
    private final ProgramService programService;

    @GetMapping("/api/programs")
    public ResponseEntity<List<ProgramListResponse>> getRegisteredPrograms() {
        return ResponseEntity.ok(programService.getRegisteredPrograms());
    }

    @GetMapping("/api/programs/selections")
    public ResponseEntity<List<ProgramSelectionResponse>> getProgramSelections(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(programService.getProgramSelections(keyword));
    }

    @GetMapping("/api/programs/{programId}/episodes")
    public ResponseEntity<ProgramEpisodeResponse> getProgramEpisodes(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getProgramEpisodes(programId));
    }
}
