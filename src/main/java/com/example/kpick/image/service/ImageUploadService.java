package com.example.kpick.image.service;

import com.example.kpick.image.dto.req.CreatePresignedUrlRequest;
import com.example.kpick.image.dto.res.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Presigner s3Presigner;

    @Value("${storage.s3.bucket:}")
    private String bucket;

    @Value("${storage.s3.region:ap-northeast-2}")
    private String region;

    @Value("${storage.s3.public-base-url:}")
    private String publicBaseUrl;

    @Value("${storage.s3.presigned-url-expiration-seconds:300}")
    private long expirationSeconds;

    public PresignedUrlResponse createPresignedUrl(CreatePresignedUrlRequest request) {
        validateRequest(request);
        String objectKey = createObjectKey(request);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(request.getContentType())
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        return new PresignedUrlResponse(
                objectKey,
                uploadUrl,
                createImageUrl(objectKey),
                "PUT",
                request.getContentType(),
                expirationSeconds
        );
    }

    private String createObjectKey(CreatePresignedUrlRequest request) {
        String extension = ALLOWED_CONTENT_TYPES.get(request.getContentType());
        return request.getUploadPurpose().getDirectory() + "/" + UUID.randomUUID() + "." + extension;
    }

    private String createImageUrl(String objectKey) {
        String baseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : publicBaseUrl.trim().replaceAll("/+$", "");
        return baseUrl + "/" + objectKey;
    }

    private void validateRequest(CreatePresignedUrlRequest request) {
        if (request == null) throw new IllegalArgumentException("Request body is required.");
        if (request.getUploadPurpose() == null) throw new IllegalArgumentException("uploadPurpose is required.");
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new IllegalArgumentException("fileName is required.");
        }
        if (!ALLOWED_CONTENT_TYPES.containsKey(request.getContentType())) {
            throw new IllegalArgumentException("contentType must be image/jpeg, image/png, or image/webp.");
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new IllegalArgumentException("fileSize must be positive.");
        }
        if (request.getFileSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file must be 10MB or less.");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("storage.s3.bucket must be configured.");
        }
    }
}
