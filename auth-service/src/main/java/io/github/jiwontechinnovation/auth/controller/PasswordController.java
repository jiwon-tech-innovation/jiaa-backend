package io.github.jiwontechinnovation.auth.controller;

import io.github.jiwontechinnovation.auth.dto.*;
import io.github.jiwontechinnovation.auth.service.PasswordService;
import io.github.jiwontechinnovation.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "비밀번호", description = "비밀번호 찾기/재설정 API")
public class PasswordController {
    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 찾기")
    public ApiResponse<Void> findPassword(@Valid @RequestBody FindPasswordRequest request) {
        passwordService.sendPasswordResetCode(request.email());
        return ApiResponse.success("비밀번호 재설정 인증 코드가 이메일로 전송되었습니다", null);
    }

    @PostMapping("/verify-password-reset")
    @Operation(summary = "비밀번호 재설정 인증 코드 확인")
    public ApiResponse<Boolean> verifyPasswordReset(@Valid @RequestBody VerifyPasswordResetRequest request) {
        boolean verified = passwordService.verifyPasswordResetCode(request.email(), request.code());
        if (verified) {
            return ApiResponse.success("인증 코드 확인이 완료되었습니다", true);
        } else {
            return new ApiResponse<>(false, "인증 코드가 올바르지 않거나 만료되었습니다", false);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request.email(), request.code(), request.newPassword());
        return ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다", null);
    }
}
