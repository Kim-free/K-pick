package com.example.kpick.ranking.controller;

import com.example.kpick.ranking.dto.res.GrowthRecordResponse;
import com.example.kpick.ranking.dto.res.MyRankingResponse;
import com.example.kpick.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<Object> getRankings(
            @RequestParam(defaultValue = "SEASON") String rankingType,
            @RequestAttribute("authenticatedProfileId") Long profileId
    ) {
        return ResponseEntity.ok(rankingService.getRankings(rankingType, profileId));
    }

    @GetMapping("/profiles/{profileId}")
    public ResponseEntity<MyRankingResponse> getRankingProfile(@PathVariable Long profileId) {
        return ResponseEntity.ok(rankingService.getRankingProfile(profileId));
    }

    @GetMapping("/profiles/me/growth")
    public ResponseEntity<GrowthRecordResponse> getGrowthRecord(
            @RequestAttribute("authenticatedProfileId") Long profileId,
            @RequestParam(defaultValue = "TOTAL") String pointScope
    ) {
        return ResponseEntity.ok(rankingService.getGrowthRecord(profileId, pointScope));
    }
}
