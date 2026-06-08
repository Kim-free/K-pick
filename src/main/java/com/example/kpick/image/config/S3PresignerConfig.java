package com.example.kpick.image.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3PresignerConfig {
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
            @Value("${storage.s3.access-key:}") String accessKey,
            @Value("${storage.s3.secret-key:}") String secretKey
    ) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new IllegalStateException("storage.s3.access-key must be configured.");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("storage.s3.secret-key must be configured.");
        }
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())
        );
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(
            AwsCredentialsProvider credentialsProvider,
            @Value("${storage.s3.region:ap-northeast-2}") String region,
            @Value("${storage.s3.endpoint:}") String endpoint
    ) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }
}
