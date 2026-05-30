package com.example.kpick.ranking.controller;

import com.example.kpick.ranking.dto.res.SeasonCloseResponse;
import com.example.kpick.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/rankings")
public class RankingAdminController {

    private final RankingService rankingService;

    @PatchMapping("/current-season/close")
    public ResponseEntity<SeasonCloseResponse> closeCurrentSeason() {
        return ResponseEntity.ok(rankingService.closeCurrentSeason());
    }
}
