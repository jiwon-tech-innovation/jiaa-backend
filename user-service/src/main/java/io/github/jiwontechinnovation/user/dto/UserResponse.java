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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getAvatarId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

