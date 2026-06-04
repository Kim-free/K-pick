package com.example.kpick.notification.dto.res;

import com.example.kpick.notification.domain.PushDeviceToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushDeviceTokenResponse {
    private Long pushDeviceTokenId;
    private String platform;

    public static PushDeviceTokenResponse from(PushDeviceToken pushDeviceToken) {
        return new PushDeviceTokenResponse(pushDeviceToken.getId(), pushDeviceToken.getPlatform());
    }
}
