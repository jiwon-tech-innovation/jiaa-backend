package io.github.jiwontechinnovation.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Auth Service
                                .route("auth-service", r -> r
                                                .path("/api/v1/auth/**")
                                                .uri("lb://auth-service"))
                                .route("auth-service-swagger", r -> r
                                                .path("/auth-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://auth-service"))
                                // User Service
                                .route("user-service", r -> r
                                                .path("/api/v1/users/**")
                                                .uri("lb://user-service"))
                                .route("user-service-swagger", r -> r
                                                .path("/user-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://user-service"))
                                // Goal Service
                                .route("goal-service", r -> r
                                                .path("/api/goal/**")
                                                .filters(f -> f.stripPrefix(2))
                                                .uri("lb://goal-service"))
                                .route("goal-service-swagger", r -> r
                                                .path("/goal-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://goal-service"))
                                // Analysis Service
                                .route("analysis-service", r -> r
                                                .path("/api/analysis/**")
                                                .filters(f -> f.stripPrefix(2))
                                                .uri("lb://analysis-service"))
                                .route("analysis-service-swagger", r -> r
                                                .path("/analysis-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://analysis-service"))
                                // AI Chat Service (FastAPI) - WebSocket (우선순위 높음, 더 구체적인 경로 먼저)
                                .route("ai-chat-service-websocket", r -> r
                                                .path("/api/v1/chat/ws")
                                                .uri("lb://ai-chat-service"))
                                // AI Chat Service (FastAPI) - HTTP 요청 (WebSocket 제외)
                                .route("ai-chat-service", r -> r
                                                .path("/api/v1/chat/**", "/api/v1/roadmaps/**",
                                                                "/api/v1/personalities/**", "/api/v1/sessions/**",
                                                                "/api/v1/debug/**")
                                                .uri("lb://ai-chat-service"))
                                .route("ai-chat-service-openapi", r -> r
                                                .path("/ai-chat-service/openapi.json")
                                                .filters(f -> f.rewritePath("/ai-chat-service/openapi.json",
                                                                "/openapi.json"))
                                                .uri("lb://ai-chat-service"))
                                // AI Judge Service (FastAPI) - Eureka Load Balancer
                                .route("ai-judge-service", r -> r
                                                .path("/api/v1/judge/**")
                                                .uri("lb://ai-judge-service"))
                                .route("ai-judge-service-openapi", r -> r
                                                .path("/ai-judge-service/openapi.json")
                                                .filters(f -> f.rewritePath("/ai-judge-service/openapi.json",
                                                                "/openapi.json"))
                                                .uri("lb://ai-judge-service"))
                                .build();
        }
}
