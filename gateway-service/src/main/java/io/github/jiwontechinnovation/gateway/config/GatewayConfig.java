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
                                // Auth Service - /api/auth/** -> /auth/**
                                .route("auth-service", r -> r
                                                .path("/api/auth/**")
                                                .filters(f -> f.rewritePath("/api/auth/(?<segment>.*)",
                                                                "/auth/${segment}"))
                                                .uri("lb://auth-service"))
                                .route("auth-service-swagger", r -> r
                                                .path("/auth-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://auth-service"))
                                // User Service - /api/users/** -> /users/**
                                .route("user-service", r -> r
                                                .path("/api/users/**")
                                                .filters(f -> f.rewritePath("/api/users/(?<segment>.*)",
                                                                "/users/${segment}"))
                                                .uri("lb://user-service"))
                                .route("user-service-swagger", r -> r
                                                .path("/user-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://user-service"))
                                // Goal Service - /api/goal/** -> /goal/**
                                .route("goal-service", r -> r
                                                .path("/api/goal/**")
                                                .filters(f -> f.rewritePath("/api/goal/(?<segment>.*)",
                                                                "/goal/${segment}"))
                                                .uri("lb://goal-service"))
                                .route("goal-service-swagger", r -> r
                                                .path("/goal-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://goal-service"))
                                // Analysis Service - /api/analysis/** -> /analysis/**
                                .route("analysis-service", r -> r
                                                .path("/api/analysis/**")
                                                .filters(f -> f.rewritePath("/api/analysis/(?<segment>.*)",
                                                                "/analysis/${segment}"))
                                                .uri("lb://analysis-service"))
                                .route("analysis-service-swagger", r -> r
                                                .path("/analysis-service/v3/api-docs/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("lb://analysis-service"))
                                // AI Chat Service (FastAPI) - WebSocket
                                .route("ai-chat-service-websocket", r -> r
                                                .path("/api/chat/ws")
                                                .filters(f -> f.rewritePath("/api/chat/ws", "/chat/ws"))
                                                .uri("lb://ai-chat-service"))
                                // AI Chat Service (FastAPI) - HTTP
                                .route("ai-chat-service", r -> r
                                                .path("/api/chat/**", "/api/roadmaps/**",
                                                                "/api/personalities/**", "/api/sessions/**",
                                                                "/api/debug/**")
                                                .filters(f -> f.rewritePath("/api/(?<segment>.*)", "/${segment}"))
                                                .uri("lb://ai-chat-service"))
                                .route("ai-chat-service-openapi", r -> r
                                                .path("/ai-chat-service/openapi.json")
                                                .filters(f -> f.rewritePath("/ai-chat-service/openapi.json",
                                                                "/openapi.json"))
                                                .uri("lb://ai-chat-service"))
                                // AI Judge Service (FastAPI)
                                .route("ai-judge-service", r -> r
                                                .path("/api/judge/**")
                                                .filters(f -> f.rewritePath("/api/judge/(?<segment>.*)",
                                                                "/judge/${segment}"))
                                                .uri("http://jiaa-ai-judge-service-svc:8080"))
                                .route("ai-judge-service-openapi", r -> r
                                                .path("/ai-judge-service/openapi.json")
                                                .filters(f -> f.rewritePath("/ai-judge-service/openapi.json",
                                                                "/openapi.json"))
                                                .uri("http://jiaa-ai-judge-service-svc:8080"))
                                .build();
        }
}
