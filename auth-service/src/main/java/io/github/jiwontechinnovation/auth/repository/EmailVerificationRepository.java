package io.github.jiwontechinnovation.auth.repository;

import io.github.jiwontechinnovation.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndCodeAndVerifiedFalse(String email, String code);

    Optional<EmailVerification> findByEmailAndVerifiedFalse(String email);

    boolean existsByEmailAndVerifiedTrue(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerification e WHERE e.email = :email AND e.verified = false")
    void deleteUnverifiedByEmail(@Param("email") String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE EmailVerification e SET e.verified = true WHERE e.email = :email AND e.code = :code AND e.verified = false AND e.expiresAt >= :now")
    int markAsVerified(@Param("email") String email, @Param("code") String code, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(e) > 0 FROM EmailVerification e WHERE e.email = :email AND e.code = :code AND e.verified = true AND e.expiresAt >= :now")
    boolean existsByEmailAndCodeAndVerifiedTrueAndNotExpired(@Param("email") String email, @Param("code") String code,
            @Param("now") LocalDateTime now);
}
