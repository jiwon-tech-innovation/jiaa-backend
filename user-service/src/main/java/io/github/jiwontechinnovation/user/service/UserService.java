package io.github.jiwontechinnovation.user.service;

import io.github.jiwontechinnovation.user.dto.UpdateAvatarRequest;
import io.github.jiwontechinnovation.user.dto.UserResponse;
import io.github.jiwontechinnovation.user.entity.User;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateAvatar(String identifier, UpdateAvatarRequest request) {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));
        
        user.setAvatarId(request.avatarId());
        User savedUser = userRepository.save(user);
        
        return UserResponse.from(savedUser);
    }
}

