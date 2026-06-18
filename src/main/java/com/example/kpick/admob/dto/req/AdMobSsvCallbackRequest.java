package com.example.kpick.admob.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdMobSsvCallbackRequest {
    private Long userId;
    private Long rewardAmount;
    private String rewardItem;
    private String adNetwork;
    private String adUnit;
    private String transactionId;
    private String signature;
    private String keyId;
    private Long timestamp;

    public static AdMobSsvCallbackRequest from(Map<String, String> queryParameters) {
        String userId = required(queryParameters, "user_id");
        String rewardAmount = required(queryParameters, "reward_amount");
        String rewardItem = required(queryParameters, "reward_item");
        String adNetwork = required(queryParameters, "ad_network");
        String adUnit = required(queryParameters, "ad_unit");
        String transactionId = required(queryParameters, "transaction_id");
        String signature = required(queryParameters, "signature");
        String keyId = required(queryParameters, "key_id");
        String timestamp = required(queryParameters, "timestamp");

        return new AdMobSsvCallbackRequest(
                parseLong(userId, "user_id"),
                parsePositiveLong(rewardAmount, "reward_amount"),
                rewardItem,
                adNetwork,
                adUnit,
                transactionId,
                signature,
                keyId,
                parseLong(timestamp, "timestamp")
        );
    }

    private static String required(Map<String, String> queryParameters, String fieldName) {
        String value = queryParameters.get(fieldName);
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
