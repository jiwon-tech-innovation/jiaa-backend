package io.github.jiwontechinnovation.auth.service;

import io.github.jiwontechinnovation.auth.dto.RefreshTokenRequest;
import io.github.jiwontechinnovation.auth.dto.SignInRequest;
import io.github.jiwontechinnovation.auth.jwt.JwtTokenProvider;
import io.github.jiwontechinnovation.user.entity.User;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignInService {
    private static final Logger logger = LoggerFactory.getLogger(SignInService.class);
    private static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";
    private static final String ERROR_INVALID_REFRESH_TOKEN = "Invalid refresh token";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public SignInService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

    @Transactional(readOnly = true)
    public TokenPair signIn(SignInRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException(ERROR_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException(ERROR_INVALID_CREDENTIALS);
        }

        logger.info("로그인 성공 - username: {}", user.getUsername());
        return createTokenPair(user.getUsername());
    }

    @Transactional(readOnly = true)
    public TokenPair refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateRefreshToken(request.refreshToken())) {
            throw new BadCredentialsException(ERROR_INVALID_REFRESH_TOKEN);
        }

        String username = jwtTokenProvider.getUsernameFromToken(request.refreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(ERROR_INVALID_REFRESH_TOKEN));

        logger.info("토큰 갱신 성공 - username: {}", username);
        return createTokenPair(user.getUsername());
    }

    private TokenPair createTokenPair(String username) {
        return new TokenPair(jwtTokenProvider.createAccessToken(username),
                jwtTokenProvider.createRefreshToken(username));
    }
}
