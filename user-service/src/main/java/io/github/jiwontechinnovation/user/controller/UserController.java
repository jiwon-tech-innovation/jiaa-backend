package io.github.jiwontechinnovation.user.controller;

import io.github.jiwontechinnovation.user.dto.UpdateAvatarRequest;
import io.github.jiwontechinnovation.user.dto.UserResponse;
import io.github.jiwontechinnovation.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰으로 인증된 현재 사용자의 정보를 조회합니다")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal String identifier) {
        return ResponseEntity.ok(userService.getCurrentUser(identifier));
    }

    @PatchMapping("/me/avatar")
    @Operation(summary = "아바타 변경", description = "현재 사용자의 아바타를 변경합니다")
    public ResponseEntity<UserResponse> updateAvatar(
            @AuthenticationPrincipal String identifier,
            @Valid @RequestBody UpdateAvatarRequest request) {
        return ResponseEntity.ok(userService.updateAvatar(identifier, request));
    }
}

