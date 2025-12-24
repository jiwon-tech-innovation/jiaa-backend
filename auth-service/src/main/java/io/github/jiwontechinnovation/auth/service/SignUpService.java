package io.github.jiwontechinnovation.auth.service;

import io.github.jiwontechinnovation.auth.dto.SignUpRequest;
import io.github.jiwontechinnovation.auth.dto.SignUpResponse;
import io.github.jiwontechinnovation.auth.dto.VerifyEmailRequest;
import io.github.jiwontechinnovation.user.entity.User;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignUpService {
    private static final Logger logger = LoggerFactory.getLogger(SignUpService.class);
    private static final String ERROR_SIGNUP_FAILED = "회원가입에 실패했습니다";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public SignUpService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다");
        }
        emailVerificationService.sendVerificationCode(email);
    }

    @Transactional(readOnly = true)
    public boolean verifyEmail(VerifyEmailRequest request) {
        return emailVerificationService.verifyCode(request.email(), request.code());
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException(ERROR_SIGNUP_FAILED);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException(ERROR_SIGNUP_FAILED);
        }
        if (!emailVerificationService.checkEmailVerified(request.email())) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), encodedPassword, request.name());
        User savedUser = userRepository.save(user);
        logger.info("회원가입 성공 - username: {}, email: {}", savedUser.getUsername(), savedUser.getEmail());
        return new SignUpResponse(savedUser.getUsername(), savedUser.getEmail());
    }
}
