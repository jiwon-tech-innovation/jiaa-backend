package io.github.jiwontechinnovation.auth.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank(message = "이메일은 필수입니다") @Email(message = "유효한 이메일 형식이 아닙니다") String email,

        @NotBlank(message = "인증 코드는 필수입니다") @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다") String code,

        @NotBlank(message = "비밀번호는 필수입니다") @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다") String newPassword) {
}
