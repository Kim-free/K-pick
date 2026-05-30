package com.example.kpick.appUser.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password; // LOCAL만 값 있음, OAuth는 null

    @Enumerated(EnumType.STRING)
    private LoginType loginType; // LOCAL, KAKAO, APPLE

    private String providerId; // OAuth만 값 있음

    private Boolean emailVerified;

    public static AppUser createOAuthUser(String email, LoginType loginType, String providerId, Boolean emailVerified) {
        return AppUser.builder()
                .email(email)
                .password(null)
                .loginType(loginType)
                .providerId(providerId)
                .emailVerified(emailVerified)
                .build();
    }
}
