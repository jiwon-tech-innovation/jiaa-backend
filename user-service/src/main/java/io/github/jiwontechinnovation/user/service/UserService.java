package io.github.jiwontechinnovation.user.service;

import io.github.jiwontechinnovation.user.dto.UpdateAvatarRequest;
import io.github.jiwontechinnovation.user.dto.UpdateProfileRequest;
import io.github.jiwontechinnovation.user.dto.UserResponse;
import io.github.jiwontechinnovation.user.entity.User;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final io.github.jiwontechinnovation.user.repository.AvatarRepository avatarRepository;
    private final io.github.jiwontechinnovation.user.repository.PersonalityRepository personalityRepository;

    public UserService(UserRepository userRepository,
            io.github.jiwontechinnovation.user.repository.AvatarRepository avatarRepository,
            io.github.jiwontechinnovation.user.repository.PersonalityRepository personalityRepository) {
        this.userRepository = userRepository;
        this.avatarRepository = avatarRepository;
        this.personalityRepository = personalityRepository;
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

        io.github.jiwontechinnovation.user.entity.Avatar avatar = avatarRepository.findById(request.avatarId())
                .orElseThrow(() -> new IllegalArgumentException("아바타를 찾을 수 없습니다: " + request.avatarId()));

        user.setAvatar(avatar);
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updatePersonality(String identifier,
            io.github.jiwontechinnovation.user.dto.UpdatePersonalityRequest request) {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));

        io.github.jiwontechinnovation.user.entity.Personality personality = personalityRepository
                .findById(request.personalityId())
                .orElseThrow(() -> new IllegalArgumentException("성격을 찾을 수 없습니다: " + request.personalityId()));

        user.setPersonality(personality);
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse updateProfile(String identifier, UpdateProfileRequest request) {
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + identifier));

        user.setName(request.name());
        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public java.util.List<io.github.jiwontechinnovation.user.entity.Avatar> getAllAvatars() {
        return avatarRepository.findAll();
    }

    @Transactional(readOnly = true)
    public java.util.List<io.github.jiwontechinnovation.user.entity.Personality> getAllPersonalities() {
        return personalityRepository.findAll();
    }
}
