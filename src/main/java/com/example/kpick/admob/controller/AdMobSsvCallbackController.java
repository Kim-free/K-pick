package com.example.kpick.admob.controller;

import com.example.kpick.admob.dto.req.AdMobSsvCallbackRequest;
import com.example.kpick.admob.service.AdMobSsvCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdMobSsvCallbackController {
    private final AdMobSsvCallbackService adMobSsvCallbackService;

    @Operation(summary = "AdMob Rewarded Ad SSV 콜백", description = "AdMob 서버가 보상형 광고 시청 완료 후 호출하는 공개 GET 콜백입니다.")
    @GetMapping("/v1/ads/admob/callback")
    public ResponseEntity<Void> handleAdMobSsvCallback(
            @Parameter(description = "Flutter customData로 전달한 유저 고유 ID. 현재 서버에서는 Profile ID 숫자 문자열을 사용합니다.", example = "1")
            @RequestParam("user_id") String userId,
            @Parameter(description = "AdMob 콘솔에 설정한 보상 수량", example = "10")
            @RequestParam("reward_amount") String rewardAmount,
            @Parameter(description = "AdMob 콘솔에 설정한 보상 항목 이름", example = "Coin")
            @RequestParam("reward_item") String rewardItem,
            @Parameter(description = "광고 네트워크 ID", example = "admob")
            @RequestParam("ad_network") String adNetwork,
            @Parameter(description = "시청한 광고 단위 ID", example = "ca-app-pub-xxx/yyy")
            @RequestParam("ad_unit") String adUnit,
            @Parameter(description = "AdMob 고유 트랜잭션 ID", example = "transaction-123")
            @RequestParam("transaction_id") String transactionId,
            @Parameter(description = "서명 검증용 데이터")
            @RequestParam("signature") String signature,
            @Parameter(description = "Google 공개 키 식별자", example = "123")
            @RequestParam("key_id") String keyId,
            @Parameter(description = "콜백 생성 timestamp", example = "1760000000000")
            @RequestParam("timestamp") String timestamp
    ) {
        adMobSsvCallbackService.handleCallback(AdMobSsvCallbackRequest.of(
                userId,
                rewardAmount,
                rewardItem,
                adNetwork,
                adUnit,
                transactionId,
                signature,
                keyId,
                timestamp
        ));
        return ResponseEntity.ok().build();
    }
}
