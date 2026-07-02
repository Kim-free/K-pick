package com.example.kpick.auth.filter;

import com.example.kpick.appUser.domain.AppUserRole;
import com.example.kpick.appUser.repository.AppUserRepository;
import com.example.kpick.auth.service.JwtClaims;
import com.example.kpick.auth.service.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    public static final String APP_USER_ID_ATTRIBUTE = "authenticatedAppUserId";
    public static final String PROFILE_ID_ATTRIBUTE = "authenticatedProfileId";
    public static final String APP_USER_ROLE_ATTRIBUTE = "authenticatedAppUserRole";

    private final JwtProvider jwtProvider;
    private final AppUserRepository appUserRepository;

    @Value("${auth.dev-bypass.enabled:false}")
    private boolean devBypassEnabled;

    @Value("${auth.dev-bypass.app-user-id:1}")
    private Long devBypassAppUserId;

    @Value("${auth.dev-bypass.profile-id:1}")
    private Long devBypassProfileId;

    @Value("${auth.dev-bypass.role:ADMIN}")
    private AppUserRole devBypassRole;

    public JwtAuthorizationFilter(JwtProvider jwtProvider, AppUserRepository appUserRepository) {
        this.jwtProvider = jwtProvider;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (devBypassEnabled) {
                request.setAttribute(APP_USER_ID_ATTRIBUTE, devBypassAppUserId);
                request.setAttribute(PROFILE_ID_ATTRIBUTE, devBypassProfileId);
                request.setAttribute(APP_USER_ROLE_ATTRIBUTE, devBypassRole);
                filterChain.doFilter(request, response);
                return;
            }
            String accessToken = extractBearerToken(request);
            JwtClaims claims = jwtProvider.parseAndValidate(accessToken);
            if (isWithdrawnAppUser(claims)) {
                writeUnauthorizedResponse(response, "Withdrawn app user.");
                return;
            }
            if (isAdminPath(request) && claims.getAppUserRole() != AppUserRole.ADMIN) {
                writeForbiddenResponse(response, "Admin role is required.");
                return;
            }
            request.setAttribute(APP_USER_ID_ATTRIBUTE, claims.getAppUserId());
            request.setAttribute(PROFILE_ID_ATTRIBUTE, claims.getProfileId());
            request.setAttribute(APP_USER_ROLE_ATTRIBUTE, claims.getAppUserRole());
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            writeUnauthorizedResponse(response, exception.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/auth/")
                || path.startsWith("/v1/ads/admob/callback")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/error");
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization Bearer token is required.");
        }
        return authorization.substring(7).trim();
    }

    private boolean isAdminPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/admin/");
    }

    private boolean isWithdrawnAppUser(JwtClaims claims) {
        return appUserRepository.findById(claims.getAppUserId())
                .map(appUser -> appUser.isWithdrawn())
                .orElse(true);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeJsonError(response, message);
    }

    private void writeForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeJsonError(response, message);
    }

    private void writeJsonError(HttpServletResponse response, String message) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        return value == null ? "Unauthorized" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
