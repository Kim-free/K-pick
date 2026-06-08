package com.example.kpick.auth.service;

import com.example.kpick.appUser.domain.AppUser;
import com.example.kpick.appUser.domain.LoginType;
import org.springframework.beans.factory.annotation.Value;
import com.example.kpick.appUser.repository.AppUserRepository;
import com.example.kpick.auth.dto.req.OAuthLoginRequest;
import com.example.kpick.auth.dto.req.TestTokenRequest;
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
    private final OAuthTokenClient oAuthTokenClient;
    private final OAuthUserInfoClient oAuthUserInfoClient;
    private final JwtProvider jwtProvider;

    @Value("${admin.emails:}")
    private String adminEmails;

    @Value("${auth.test-token.enabled:false}")
    private boolean testTokenEnabled;

    @Transactional
    public AuthResponse loginWithApple(OAuthLoginRequest request) {
        validateOAuthLoginRequest(request);
        OAuthTokenResponse tokenResponse = oAuthTokenClient.exchangeAppleCode(request.getAuthorizationCode());
        AppleTokenClaims claims = appleIdentityTokenParser.parseAndValidate(tokenResponse.getIdentityToken());
        return loginOAuth(LoginType.APPLE, new OAuthProviderProfile(
                claims.getSubject(),
                claims.getEmail(),
                claims.getEmailVerified()
        ));
    }

    @Transactional
    public AuthResponse loginWithGoogle(OAuthLoginRequest request) {
        validateOAuthLoginRequest(request);
        OAuthTokenResponse tokenResponse = oAuthTokenClient.exchangeGoogleCode(request.getAuthorizationCode());
        return loginOAuth(LoginType.GOOGLE, oAuthUserInfoClient.getGoogleProfile(tokenResponse.getAccessToken()));
    }

    @Transactional
    public AuthResponse loginWithKakao(OAuthLoginRequest request) {
        validateOAuthLoginRequest(request);
        OAuthTokenResponse tokenResponse = oAuthTokenClient.exchangeKakaoCode(request.getAuthorizationCode());
        return loginOAuth(LoginType.KAKAO, oAuthUserInfoClient.getKakaoProfile(tokenResponse.getAccessToken()));
    }

    @Transactional
    public AuthResponse createTestToken(TestTokenRequest request) {
        if (!testTokenEnabled) {
            throw new IllegalStateException("Test token API is disabled.");
        }
        if (request == null || request.getAppUserId() == null) {
            throw new IllegalArgumentException("appUserId is required.");
        }
        AppUser appUser = appUserRepository.findById(request.getAppUserId())
                .orElseThrow(() -> new IllegalArgumentException("AppUser not found. appUserId=" + request.getAppUserId()));
        validateActiveAppUser(appUser);
        appUser.markLoggedIn();
        Profile profile = getOrCreateProfile(appUser.getId());
        String accessToken = jwtProvider.createAccessToken(appUser, profile.getId());
        return new AuthResponse(
                appUser.getId(),
                profile.getId(),
                accessToken,
                "Bearer",
                false,
                profile.getSignUpStatus(),
                appUser.getRoleOrDefault()
        );
    }

    private AuthResponse loginOAuth(LoginType loginType, OAuthProviderProfile providerProfile) {
        AppUser appUser = appUserRepository.findByLoginTypeAndProviderId(loginType, providerProfile.getProviderId())
                .orElse(null);
        boolean newUser = appUser == null;
        if (!newUser) {
            validateActiveAppUser(appUser);
        }

        if (newUser) {
            appUser = appUserRepository.save(AppUser.createOAuthUser(
                    providerProfile.getEmail(),
                    loginType,
                    providerProfile.getProviderId(),
                    providerProfile.getEmailVerified()
            ));
        }
        appUser.markLoggedIn();
        grantAdminRoleIfConfigured(appUser, providerProfile.getEmail());

        Long appUserId = appUser.getId();
        Profile profile = getOrCreateProfile(appUserId);

        String accessToken = jwtProvider.createAccessToken(appUser, profile.getId());
        return new AuthResponse(
                appUser.getId(),
                profile.getId(),
                accessToken,
                "Bearer",
                newUser,
                profile.getSignUpStatus(),
                appUser.getRoleOrDefault()
        );
    }

    private Profile getOrCreateProfile(Long appUserId) {
        return profileRepository.findByAppUserId(appUserId)
                .orElseGet(() -> profileRepository.save(Profile.builder()
                        .appUserId(appUserId)
                        .coin(0L)
                        .missionPoint(0L)
                        .totalMissionPoint(0L)
                        .activityPoint(0L)
                        .signUpStatus(SignUpStatus.NULL)
                        .build()));
    }

    private void grantAdminRoleIfConfigured(AppUser appUser, String email) {
        if (email == null || email.isBlank() || adminEmails == null || adminEmails.isBlank()) {
            return;
        }
        boolean isAdminEmail = java.util.Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .anyMatch(value -> value.equalsIgnoreCase(email.trim()));
        if (isAdminEmail) {
            appUser.grantAdminRole();
        }
    }

    private void validateOAuthLoginRequest(OAuthLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getAuthorizationCode() == null || request.getAuthorizationCode().isBlank()) {
            throw new IllegalArgumentException("authorizationCode is required.");
        }
    }

    private void validateActiveAppUser(AppUser appUser) {
        if (appUser.isWithdrawn()) {
            throw new IllegalArgumentException("Withdrawn app user.");
        }
    }
}
