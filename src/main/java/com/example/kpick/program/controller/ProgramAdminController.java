package com.example.kpick.program.controller;

import com.example.kpick.program.dto.req.CreateProgramRequest;
import com.example.kpick.program.dto.req.UpdateProgramRequest;
import com.example.kpick.program.dto.res.ProgramDetailsResponse;
import com.example.kpick.program.dto.res.AdminProgramListResponse;
import com.example.kpick.program.dto.res.ProgramResponse;
import com.example.kpick.program.domain.Genre;
import com.example.kpick.program.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/programs")
public class ProgramAdminController {

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(@RequestBody CreateProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programService.createProgram(request));
    }

    @GetMapping
    public ResponseEntity<List<AdminProgramListResponse>> getPrograms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) Boolean isOnAir,
            @RequestParam(required = false) Boolean isExposed
    ) {
        return ResponseEntity.ok(programService.getAdminPrograms(keyword, genre, isOnAir, isExposed));
    }

    @GetMapping("/{programId}/details")
    public ResponseEntity<ProgramDetailsResponse> getProgramDetails(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getProgramDetails(programId));
    }

    @PatchMapping("/{programId}")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable Long programId,
            @RequestBody UpdateProgramRequest request
    ) {
        return ResponseEntity.ok(programService.updateProgram(programId, request));
    }

    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ResponseEntity.noContent().build();
    }
}
