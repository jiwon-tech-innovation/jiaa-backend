package io.github.jiwontechinnovation.auth.controller;

import io.github.jiwontechinnovation.auth.dto.*;
import io.github.jiwontechinnovation.auth.service.SignUpService;
import io.github.jiwontechinnovation.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "회원가입", description = "회원가입/이메일 인증 API")
public class SignUpController {
    private final SignUpService signUpService;
    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    @PostMapping("/send-verification-code")
    @Operation(summary = "이메일 인증 코드 전송")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        signUpService.sendVerificationCode(request.email());
        return ApiResponse.success("인증 코드가 이메일로 전송되었습니다", null);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증")
    public ApiResponse<Boolean> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        boolean verified = signUpService.verifyEmail(request);
        if (verified) {
            return ApiResponse.success("이메일 인증이 완료되었습니다", true);
        } else {
            return new ApiResponse<>(false, "인증 코드가 올바르지 않거나 만료되었습니다", false);
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ApiResponse.success("회원가입 성공", signUpService.signUp(request));
    }
}
