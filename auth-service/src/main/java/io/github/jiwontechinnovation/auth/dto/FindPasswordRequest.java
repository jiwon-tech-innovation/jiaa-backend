package io.github.jiwontechinnovation.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindPasswordRequest(
        @NotBlank(message = "이메일은 필수입니다") @Email(message = "유효한 이메일 형식이 아닙니다") String email) {
}
