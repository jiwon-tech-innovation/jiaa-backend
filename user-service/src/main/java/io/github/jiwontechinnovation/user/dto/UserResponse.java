package io.github.jiwontechinnovation.user.dto;

import io.github.jiwontechinnovation.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String name,
        String avatarId,
        String avatarName,
        String avatarUrl,
        String personalityId,
        String personalityName,
        String personalityDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getAvatar() != null ? user.getAvatar().getId() : "default",
                user.getAvatar() != null ? user.getAvatar().getName() : "Default",
                user.getAvatar() != null ? user.getAvatar().getS3Url() : "",
                user.getPersonality() != null ? user.getPersonality().getId() : "betain",
                user.getPersonality() != null ? user.getPersonality().getName() : "베타인",
                user.getPersonality() != null ? user.getPersonality().getDescription() : "상냥하고 사려 깊은 친구 캐릭터",
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
