package io.github.jiwontechinnovation.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank(message = "사용자명 또는 이메일은 필수입니다") String usernameOrEmail,
        @NotBlank(message = "비밀번호는 필수입니다") String password) {
}
