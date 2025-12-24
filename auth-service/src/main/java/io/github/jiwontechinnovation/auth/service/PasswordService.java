package io.github.jiwontechinnovation.auth.service;

import io.github.jiwontechinnovation.user.entity.User;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public PasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    public void sendPasswordResetCode(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            emailVerificationService.sendPasswordResetCode(user.getEmail());
            logger.info("비밀번호 재설정 인증 코드 전송 - email: {}", email);
        });
    }

    @Transactional(readOnly = true)
    public boolean verifyPasswordResetCode(String email, String code) {
        return userRepository.findByEmail(email)
                .map(user -> emailVerificationService.verifyCode(user.getEmail(), code))
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));

        if (!emailVerificationService.verifyPasswordResetCode(email, code)) {
            throw new IllegalStateException("인증 코드가 올바르지 않거나 만료되었습니다");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("비밀번호 재설정 성공 - email: {}", email);
    }
}
