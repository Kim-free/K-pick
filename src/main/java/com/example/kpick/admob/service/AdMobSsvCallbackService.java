package com.example.kpick.admob.service;

import com.example.kpick.admob.domain.AdRewardTransaction;
import com.example.kpick.admob.dto.req.AdMobSsvCallbackRequest;
import com.example.kpick.admob.repository.AdRewardTransactionRepository;
import com.example.kpick.benefit.domain.PickHistory;
import com.example.kpick.benefit.domain.PickHistoryType;
import com.example.kpick.benefit.repository.PickHistoryRepository;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdMobSsvCallbackService {
    private final AdRewardTransactionRepository adRewardTransactionRepository;
    private final ProfileRepository profileRepository;
    private final PickHistoryRepository pickHistoryRepository;
    private final AdMobSsvVerifier adMobSsvVerifier;

    @Transactional
    public void handleCallback(AdMobSsvCallbackRequest request) {
        logCallback(request);
        if (!adMobSsvVerifier.validate(request)) {
            throw new IllegalArgumentException("Invalid AdMob SSV signature.");
        }
        if (adRewardTransactionRepository.existsByTransactionId(request.getTransactionId())) {
            log.info("Duplicate AdMob SSV callback ignored. transactionId={}", request.getTransactionId());
            return;
        }

        Profile profile = profileRepository.findById(request.getUserId()).orElse(null);
        if (profile == null) {
            // AdMob may retry non-2xx responses. Return 200 and skip reward so an invalid user_id
            // does not keep retrying indefinitely. Monitor logs for these cases.
            log.warn("AdMob SSV callback user not found. userId={}, rawUserId={}, transactionId={}", request.getUserId(), request.getRawUserId(), request.getTransactionId());
            return;
        }

        profile.addCoin(request.getRewardAmount());
        pickHistoryRepository.save(PickHistory.create(
                profile.getId(),
                profile.getNickname(),
                PickHistoryType.AD_REWARD,
                request.getRewardAmount(),
                "AdMob 광고 시청"
        ));
        saveTransaction(request);
    }

    private void saveTransaction(AdMobSsvCallbackRequest request) {
        try {
            adRewardTransactionRepository.save(AdRewardTransaction.completed(request));
        } catch (DataIntegrityViolationException exception) {
            log.info("Duplicate AdMob SSV callback ignored by unique constraint. transactionId={}", request.getTransactionId());
        }
    }

    private void logCallback(AdMobSsvCallbackRequest request) {
        log.info(
                "AdMob SSV callback received. user_id={}, parsed_profile_id={}, reward_amount={}, reward_item={}, ad_network={}, ad_unit={}, transaction_id={}, signature={}, key_id={}, timestamp={}",
                request.getRawUserId(),
                request.getUserId(),
                request.getRewardAmount(),
                request.getRewardItem(),
                request.getAdNetwork(),
                request.getAdUnit(),
                request.getTransactionId(),
                maskSignature(request.getSignature()),
                request.getKeyId(),
                request.getTimestamp()
        );
    }

    private String maskSignature(String signature) {
        if (signature == null || signature.length() <= 12) {
            return "****";
        }
        return signature.substring(0, 6) + "..." + signature.substring(signature.length() - 6);
    }
}
