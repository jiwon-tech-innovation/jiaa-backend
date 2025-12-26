package io.github.jiwontechinnovation.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKeyString) {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Object typeClaim = claims.get("type");
            String type = String.valueOf(typeClaim);
            boolean isAccess = "access".equals(type);
            boolean isNotExpired = claims.getExpiration().after(new Date());
            
            logger.debug("Token validation - type: {}, isAccess: {}, isNotExpired: {}", type, isAccess, isNotExpired);
            
            return isAccess && isNotExpired;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException | SignatureException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during token validation: {}", e.getMessage(), e);
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

