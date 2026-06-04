package com.example.kpick.ranking.controller;

import com.example.kpick.ranking.dto.req.CreateRankingSeasonRequest;
import com.example.kpick.ranking.dto.res.AdminRankingSeasonResponse;
import com.example.kpick.ranking.dto.res.AdminSeasonRewardResponse;
import com.example.kpick.ranking.dto.res.SeasonCloseResponse;
import com.example.kpick.ranking.dto.res.SeasonRewardSendResponse;
import com.example.kpick.ranking.service.RankingAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/rankings")
public class RankingAdminController {

    private final RankingAdminService rankingAdminService;

    @PatchMapping("/current-season/close")
    public ResponseEntity<SeasonCloseResponse> closeCurrentSeason() {
        return ResponseEntity.ok(rankingAdminService.closeCurrentSeason());
    }

    @PostMapping("/seasons")
    public ResponseEntity<AdminRankingSeasonResponse> createSeason(@RequestBody CreateRankingSeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rankingAdminService.createSeason(request));
    }

    @GetMapping("/seasons")
    public ResponseEntity<List<AdminRankingSeasonResponse>> getSeasons() {
        return ResponseEntity.ok(rankingAdminService.getSeasons());
    }

    @GetMapping("/seasons/{rankingSeasonId}/rewards")
    public ResponseEntity<List<AdminSeasonRewardResponse>> getSeasonRewards(@PathVariable Long rankingSeasonId) {
        return ResponseEntity.ok(rankingAdminService.getSeasonRewards(rankingSeasonId));
    }

    @PatchMapping("/seasons/{rankingSeasonId}/rewards/send-all")
    public ResponseEntity<SeasonRewardSendResponse> sendAllRewards(@PathVariable Long rankingSeasonId) {
        return ResponseEntity.ok(rankingAdminService.sendAllRewards(rankingSeasonId));
    }

    @PatchMapping("/seasons/{rankingSeasonId}/rewards/{seasonRewardId}/send")
    public ResponseEntity<AdminSeasonRewardResponse> sendReward(
            @PathVariable Long rankingSeasonId,
            @PathVariable Long seasonRewardId
    ) {
        return ResponseEntity.ok(rankingAdminService.sendReward(rankingSeasonId, seasonRewardId));
    }
}
