package io.github.jiwontechinnovation.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePersonalityRequest(
        @NotBlank(message = "성격 ID는 필수입니다") String personalityId) {
}
