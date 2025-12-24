package io.github.jiwontechinnovation.auth.filter;

import io.github.jiwontechinnovation.auth.jwt.JwtTokenProvider;
import io.github.jiwontechinnovation.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final long CACHE_EXPIRY_MS = 300_000L;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final Map<String, CachedUser> userCache = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    private record CachedUser(String username, long timestamp) {
        boolean isExpired(long now) {
            return now - timestamp > CACHE_EXPIRY_MS;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            if (isUserExists(username)) {
                var auth = new UsernamePasswordAuthenticationToken(username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isUserExists(String username) {
        long now = System.currentTimeMillis();
        CachedUser cached = userCache.get(username);
        if (cached != null && !cached.isExpired(now))
            return true;
        boolean exists = userRepository.findByUsername(username).isPresent();
        if (exists)
            userCache.put(username, new CachedUser(username, now));
        else
            userCache.remove(username);
        return exists;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }
}
