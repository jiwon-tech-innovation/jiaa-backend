package io.github.jiwontechinnovation.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAvatarRequest(
        @NotBlank(message = "아바타 ID는 필수입니다")
        @Size(max = 255, message = "아바타 ID는 255자 이하여야 합니다")
        String avatarId
) {
}

