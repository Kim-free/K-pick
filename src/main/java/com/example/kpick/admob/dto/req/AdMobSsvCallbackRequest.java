package com.example.kpick.admob.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdMobSsvCallbackRequest {
    private Long userId;
    private String rawUserId;
    private Long rewardAmount;
    private String rewardItem;
    private String adNetwork;
    private String adUnit;
    private String transactionId;
    private String signature;
    private String keyId;
    private Long timestamp;

    public static AdMobSsvCallbackRequest of(
            String userId,
            String rewardAmount,
            String rewardItem,
            String adNetwork,
            String adUnit,
            String transactionId,
            String signature,
            String keyId,
            String timestamp
    ) {
        String normalizedUserId = required(userId, "user_id");
        return new AdMobSsvCallbackRequest(
                parseLong(normalizedUserId, "user_id"),
                normalizedUserId,
                parsePositiveLong(required(rewardAmount, "reward_amount"), "reward_amount"),
                required(rewardItem, "reward_item"),
                required(adNetwork, "ad_network"),
                required(adUnit, "ad_unit"),
                required(transactionId, "transaction_id"),
                required(signature, "signature"),
                required(keyId, "key_id"),
                parseLong(required(timestamp, "timestamp"), "timestamp")
        );
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private static long parsePositiveLong(String value, String fieldName) {
        long parsedValue = parseLong(value, fieldName);
        if (parsedValue <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
        return parsedValue;
    }

    private static long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }
}
