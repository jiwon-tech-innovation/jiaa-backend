package io.github.jiwontechinnovation.auth.service;

import io.github.jiwontechinnovation.auth.entity.EmailVerification;
import io.github.jiwontechinnovation.auth.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final long EXPIRATION_MINUTES = 10L;

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
            EmailService emailService) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String sendVerificationCode(String email) {
        emailVerificationRepository.deleteUnverifiedByEmail(email);
        String code = generateVerificationCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        emailVerificationRepository.save(new EmailVerification(email, code, expiresAt));
        emailService.sendVerificationCode(email, code);
        log.info("이메일 인증 코드 생성 및 전송 - email: {}", email);
        return code;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyCode(String email, String code) {
        int updated = emailVerificationRepository.markAsVerified(email, code, LocalDateTime.now());
        if (updated > 0) {
            log.info("이메일 인증 성공 - email: {}", email);
            return true;
        }
        log.warn("이메일 인증 실패 - email: {}, code: {}", email, code);
        return false;
    }

    @Transactional(readOnly = true)
    public boolean checkEmailVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerifiedTrue(email);
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String sendPasswordResetCode(String email) {
        emailVerificationRepository.deleteUnverifiedByEmail(email);
        String code = generateVerificationCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        emailVerificationRepository.save(new EmailVerification(email, code, expiresAt));
        emailService.sendPasswordResetCode(email, code);
        log.info("비밀번호 재설정 인증 코드 생성 및 전송 - email: {}", email);
        return code;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyPasswordResetCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();
        int updated = emailVerificationRepository.markAsVerified(email, code, now);
        if (updated > 0)
            return true;
        return emailVerificationRepository.existsByEmailAndCodeAndVerifiedTrueAndNotExpired(email, code, now);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredVerifications() {
        int deleted = emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        if (deleted > 0)
            log.info("만료된 인증 코드 정리: {}개 삭제", deleted);
    }
}
