package com.example.kpick.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseMessagingConfig {
    @Bean
    @ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(
            @Value("${firebase.service-account-path}") String serviceAccountPath
    ) throws IOException {
        FirebaseApp firebaseApp = FirebaseApp.getApps().stream()
                .findFirst()
                .orElseGet(() -> initializeFirebaseApp(serviceAccountPath));
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private FirebaseApp initializeFirebaseApp(String serviceAccountPath) {
        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize Firebase Admin SDK.", exception);
        }
    }
}
