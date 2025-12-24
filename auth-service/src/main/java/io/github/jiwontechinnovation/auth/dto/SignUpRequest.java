package io.github.jiwontechinnovation.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank() String username,

        @Email String email,

        @NotBlank @Size(min = 8) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$", message = "비밀번호는 최소 8자 이상이며, 소문자, 대문자, 숫자, 특수문자를 포함해야 합니다") String password,

        @NotBlank String name) {
}
