package com.example.kpick.benefit.controller;

import com.example.kpick.benefit.domain.PickHistoryType;
import com.example.kpick.benefit.dto.req.UpdateBenefitSettingRequest;
import com.example.kpick.benefit.dto.res.BenefitSettingResponse;
import com.example.kpick.benefit.dto.res.PickHistoryResponse;
import com.example.kpick.benefit.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/benefits")
public class BenefitAdminController {
    private final BenefitService benefitService;

    @GetMapping("/pick-histories")
    public ResponseEntity<PickHistoryResponse> getPickHistories(
            @RequestParam(required = false) PickHistoryType pickHistoryType,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ResponseEntity.ok(benefitService.getPickHistories(pickHistoryType, fromDate, toDate));
    }

    @GetMapping("/setting")
    public ResponseEntity<BenefitSettingResponse> getBenefitSetting() {
        return ResponseEntity.ok(benefitService.getBenefitSetting());
    }

    @PatchMapping("/setting")
    public ResponseEntity<BenefitSettingResponse> updateBenefitSetting(
            @RequestBody UpdateBenefitSettingRequest request
    ) {
        return ResponseEntity.ok(benefitService.updateBenefitSetting(request));
    }
}
