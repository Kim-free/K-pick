package com.example.kpick.notification.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterPushDeviceTokenRequest {
    private String deviceToken;
    private String platform;
}
