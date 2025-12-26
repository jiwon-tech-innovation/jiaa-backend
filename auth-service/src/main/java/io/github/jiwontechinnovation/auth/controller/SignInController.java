package io.github.jiwontechinnovation.auth.controller;

import io.github.jiwontechinnovation.auth.dto.AuthResponse;
import io.github.jiwontechinnovation.auth.dto.RefreshTokenRequest;
import io.github.jiwontechinnovation.auth.dto.SignInRequest;
import io.github.jiwontechinnovation.auth.service.SignInService;
import io.github.jiwontechinnovation.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "로그인", description = "로그인/로그아웃/토큰 갱신 API")
public class SignInController {
    private final SignInService signInService;
    private final boolean isProduction;

    public SignInController(SignInService signInService, Environment environment) {
        this.signInService = signInService;
        this.isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인")
    public ApiResponse<AuthResponse> signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        SignInService.TokenPair tokenPair = signInService.signIn(request);
        setRefreshTokenCookie(response, tokenPair.refreshToken());
        return ApiResponse.success("로그인 성공", new AuthResponse(tokenPair.accessToken(), tokenPair.refreshToken()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신")
    public ApiResponse<AuthResponse> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
            @Valid @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response) {
        String refreshToken = refreshTokenFromCookie != null ? refreshTokenFromCookie
                : (request != null ? request.refreshToken() : null);
        if (refreshToken == null)
            throw new IllegalArgumentException("리프레시 토큰이 필요합니다");

        SignInService.TokenPair tokenPair = signInService.refreshToken(new RefreshTokenRequest(refreshToken));
        setRefreshTokenCookie(response, tokenPair.refreshToken());
        return ApiResponse.success("토큰 갱신 성공", new AuthResponse(tokenPair.accessToken(), tokenPair.refreshToken()));
    }

    @PostMapping("/signout")
    @Operation(summary = "로그아웃")
    public ApiResponse<Void> signOut(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ApiResponse.success("로그아웃되었습니다", null);
    }

    @PostMapping("/auto-signin")
    @Operation(summary = "자동 로그인 (리프레시 토큰으로 로그인)", description = "데스크톱 앱 재시작 시 저장된 리프레시 토큰으로 자동 로그인")
    public ApiResponse<AuthResponse> autoLogin(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletResponse response) {
        SignInService.TokenPair tokenPair = signInService.refreshToken(request);
        setRefreshTokenCookie(response, tokenPair.refreshToken());
        return ApiResponse.success("자동 로그인 성공", new AuthResponse(tokenPair.accessToken(), tokenPair.refreshToken()));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(refreshToken, 604800L).toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie("", 0L).toString());
    }

    private ResponseCookie createCookie(String value, long maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true).secure(isProduction).path("/").maxAge(maxAge)
                .sameSite(isProduction ? "None" : "Lax").build();
    }
}
