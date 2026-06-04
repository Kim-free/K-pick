package com.example.kpick.mission.controller;

import com.example.kpick.mission.dto.req.ConfirmMissionResultRequest;
import com.example.kpick.mission.dto.req.CreateMissionRequest;
import com.example.kpick.mission.domain.AdminMissionStatus;
import com.example.kpick.mission.dto.res.MissionAdminResponse;
import com.example.kpick.mission.dto.res.MissionResponse;
import com.example.kpick.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/missions")
public class MissionAdminController {
    private final MissionService missionService;

    @GetMapping
    public ResponseEntity<java.util.List<MissionAdminResponse>> getAdminMissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) AdminMissionStatus adminStatus
    ) {
        return ResponseEntity.ok(missionService.getAdminMissions(keyword, programId, adminStatus));
    }

    @PostMapping
    public ResponseEntity<MissionResponse> createMission(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody CreateMissionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(missionService.createMission(request.withProfileId(profileId)));
    }

    @PatchMapping("/{missionId}/confirm-result")
    public ResponseEntity<MissionResponse> confirmResult(@PathVariable Long missionId, @RequestBody ConfirmMissionResultRequest request) {
        return ResponseEntity.ok(missionService.confirmResult(missionId, request));
    }
}
