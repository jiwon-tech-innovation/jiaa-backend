package io.github.jiwontechinnovation.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessTokenValidityInMs;
    private final long refreshTokenValidityInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.access-token-validity-in-ms:3600000}") long accessTokenValidityInMs,
            @Value("${jwt.refresh-token-validity-in-ms:604800000}") long refreshTokenValidityInMs) {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    public String createAccessToken(String identifier) {
        return createToken(identifier, "access", accessTokenValidityInMs);
    }

    public String createRefreshToken(String identifier) {
        return createToken(identifier, "refresh", refreshTokenValidityInMs);
    }

    private String createToken(String identifier, String type, long validityInMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .subject(identifier)
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        return validateTokenType(token, "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, "refresh");
    }

    private boolean validateTokenType(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);
            return claims.get("type").equals(expectedType) && claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
