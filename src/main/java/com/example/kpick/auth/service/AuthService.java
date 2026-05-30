package com.example.kpick.auth.service;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.domain.LoginType;
import com.example.kpick.appUser.repository.AppUserRepository;
import com.example.kpick.auth.dto.req.AppleLoginRequest;
import com.example.kpick.auth.dto.res.AuthResponse;
import com.example.kpick.profile.domain.Profile;
import com.example.kpick.profile.domain.SignUpStatus;
import com.example.kpick.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final AppleIdentityTokenParser appleIdentityTokenParser;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse loginWithApple(AppleLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        AppleTokenClaims claims = appleIdentityTokenParser.parseAndValidate(request.getIdentityToken());
        AppUser appUser = appUserRepository.findByLoginTypeAndProviderId(LoginType.APPLE, claims.getSubject())
                .orElse(null);
        boolean newUser = appUser == null;

        if (newUser) {
            appUser = appUserRepository.save(AppUser.createOAuthUser(
                    claims.getEmail(),
                    LoginType.APPLE,
                    claims.getSubject(),
                    claims.getEmailVerified()
            ));
        }

        Long appUserId = appUser.getId();
        Profile profile = profileRepository.findByAppUserId(appUserId)
                .orElseGet(() -> profileRepository.save(Profile.builder()
                        .appUserId(appUserId)
                        .coin(0L)
                        .missionPoint(0L)
                        .totalMissionPoint(0L)
                        .activityPoint(0L)
                        .signUpStatus(SignUpStatus.NULL)
                        .build()));

        String accessToken = jwtProvider.createAccessToken(appUser, profile.getId());
        return new AuthResponse(
                appUser.getId(),
                profile.getId(),
                accessToken,
                "Bearer",
                newUser,
                profile.getSignUpStatus()
        );
    }
}
