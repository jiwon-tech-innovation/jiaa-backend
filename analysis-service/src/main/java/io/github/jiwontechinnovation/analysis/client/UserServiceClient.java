package io.github.jiwontechinnovation.analysis.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class UserServiceClient {
    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UUID getUserIdByUsername(String username, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // LoadBalanced RestTemplate을 사용하면 서비스 이름으로 직접 호출 가능
            String url = "http://user-service/users/me";

            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserResponse.class);

            if (response.getBody() != null) {
                return response.getBody().id();
            }
        } catch (Exception e) {
            // 로그만 남기고 null 반환
            System.err.println("Failed to get user ID from User Service: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public record UserResponse(UUID id, String username, String email, String name, String avatarId) {
    }
}
