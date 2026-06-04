package com.example.kpick.mission.controller;

import com.example.kpick.mission.dto.req.SelectMissionOptionRequest;
import com.example.kpick.mission.dto.res.MissionConfirmModalResponse;
import com.example.kpick.mission.dto.res.MissionRelatedContentResponse;
import com.example.kpick.mission.dto.res.MissionRecommendationResponse;
import com.example.kpick.mission.service.MissionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MissionController {

    private final MissionQueryService missionQueryService;

    @GetMapping("/missions/recommendations")
    public ResponseEntity<List<MissionRecommendationResponse>> getMissionRecommendations(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestParam(required = false) Long programId
    ) {
        return ResponseEntity.ok(missionQueryService.getMissionRecommendations(profileId, programId));
    }

    @GetMapping("/missions/{missionId}/related")
    public ResponseEntity<MissionRelatedContentResponse> getRelatedContents(@PathVariable Long missionId) {
        return ResponseEntity.ok(missionQueryService.getRelatedContents(missionId));
    }

    @GetMapping("/missions/{missionId}")
    public ResponseEntity<Object> getMissionDetails(
            @PathVariable Long missionId,
            @RequestAttribute("authenticatedProfileId") Long profileId
    ) {
        return ResponseEntity.ok(missionQueryService.getMissionDetails(missionId, profileId));
    }

    @PostMapping("/missions/{missionId}/options/select")
    public ResponseEntity<MissionConfirmModalResponse> selectMissionOption(
            @PathVariable Long missionId,
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody SelectMissionOptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(missionQueryService.selectMissionOption(missionId, request.withProfileId(profileId)));
    }

}
