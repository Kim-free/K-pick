package com.example.kpick.appUser.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    private Boolean withdrawn;
    private LocalDateTime withdrawnAt;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static AppUser createOAuthUser(String email, LoginType loginType, String providerId, Boolean emailVerified) {
        LocalDateTime now = LocalDateTime.now();
        return AppUser.builder()
                .email(email)
                .password(null)
                .loginType(loginType)
                .providerId(providerId)
                .emailVerified(emailVerified)
                .appUserRole(AppUserRole.USER)
                .withdrawn(false)
                .createdAt(now)
                .lastLoginAt(now)
                .build();
    }

    public void markLoggedIn() {
        this.lastLoginAt = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = this.lastLoginAt;
        }
        if (this.appUserRole == null) {
            this.appUserRole = AppUserRole.USER;
        }
        if (this.withdrawn == null) {
            this.withdrawn = false;
        }
    }

    public void grantAdminRole() {
        this.appUserRole = AppUserRole.ADMIN;
    }

    public AppUserRole getRoleOrDefault() {
        return this.appUserRole == null ? AppUserRole.USER : this.appUserRole;
    }

    public boolean isWithdrawn() {
        return Boolean.TRUE.equals(this.withdrawn);
    }

    public void withdraw() {
        if (isWithdrawn()) {
            return;
        }
        this.withdrawn = true;
        this.withdrawnAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.withdrawn = false;
        this.withdrawnAt = null;
    }
}
