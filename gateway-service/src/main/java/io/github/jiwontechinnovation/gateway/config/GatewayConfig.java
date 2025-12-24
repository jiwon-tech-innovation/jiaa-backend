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
                        .uri("lb://AUTH-SERVICE"))
                .route("auth-service-swagger", r -> r
                        .path("/auth-service/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://AUTH-SERVICE"))
                // User Service
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .uri("lb://USER-SERVICE"))
                .route("user-service-swagger", r -> r
                        .path("/user-service/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://USER-SERVICE"))
                // Goal Service
                .route("goal-service", r -> r
                        .path("/api/goal/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://GOAL-SERVICE"))
                .route("goal-service-swagger", r -> r
                        .path("/goal-service/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://GOAL-SERVICE"))
                // Analysis Service
                .route("analysis-service", r -> r
                        .path("/api/analysis/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://ANALYSIS-SERVICE"))
                .route("analysis-service-swagger", r -> r
                        .path("/analysis-service/v3/api-docs/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://ANALYSIS-SERVICE"))
                .build();
    }
}

