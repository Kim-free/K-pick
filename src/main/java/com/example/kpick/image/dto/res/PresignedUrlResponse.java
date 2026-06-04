package com.example.kpick.image.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {
    private String objectKey;
    private String uploadUrl;
    private String imageUrl;
    private String method;
    private String contentType;
    private long expiresInSeconds;
}
