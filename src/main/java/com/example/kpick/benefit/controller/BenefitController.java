package com.example.kpick.benefit.controller;

import com.example.kpick.benefit.dto.res.AdRewardResponse;
import com.example.kpick.benefit.dto.res.AttendanceCheckResponse;
import com.example.kpick.benefit.dto.res.BenefitHomeResponse;
import com.example.kpick.benefit.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/benefits")
public class BenefitController {
    private final BenefitService benefitService;

    @GetMapping("/me")
    public ResponseEntity<BenefitHomeResponse> getBenefitHome(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(benefitService.getBenefitHome(profileId));
    }

    @PostMapping("/me/attendance")
    public ResponseEntity<AttendanceCheckResponse> checkAttendance(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(benefitService.checkAttendance(profileId));
    }

    @PostMapping("/me/ad-reward")
    public ResponseEntity<AdRewardResponse> rewardAd(@RequestAttribute("authenticatedProfileId") Long profileId) {
        return ResponseEntity.ok(benefitService.rewardAd(profileId));
    }
}
