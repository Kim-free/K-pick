package com.example.kpick.mission.controller;

import com.example.kpick.mission.dto.req.CreateMissionSuggestionRequest;
import com.example.kpick.mission.dto.res.MissionSuggestionResponse;
import com.example.kpick.mission.service.MissionSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mission-suggestions")
public class MissionSuggestionController {
    private final MissionSuggestionService missionSuggestionService;

    @PostMapping
    public ResponseEntity<MissionSuggestionResponse> createMissionSuggestion(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestBody CreateMissionSuggestionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(missionSuggestionService.createMissionSuggestion(request.withProfileId(profileId)));
    }
}
