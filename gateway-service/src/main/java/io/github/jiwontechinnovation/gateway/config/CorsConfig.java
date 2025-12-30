package io.github.jiwontechinnovation.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();

            // 다운스트림 서비스에서 온 CORS 헤더 제거 (중복 방지)
            // Gateway에서 통일된 CORS 정책을 적용하기 위함
            headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
            headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
            headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
            headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
            headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);

            // WebSocket 업그레이드 요청은 CORS 필터를 건너뜀 (WebSocket은 자체적으로 처리)
            // Connection 헤더도 확인 (일부 클라이언트는 Upgrade만 보낼 수 있음)
            String upgradeHeader = request.getHeaders().getFirst("Upgrade");
            String connectionHeader = request.getHeaders().getFirst("Connection");
            if ("websocket".equalsIgnoreCase(upgradeHeader) ||
                    (connectionHeader != null && connectionHeader.toLowerCase().contains("upgrade"))) {
                return chain.filter(exchange);
            }

            // Origin 헤더 가져오기
            String origin = request.getHeaders().getOrigin();

            // 허용된 Origin 목록 (개발 환경)
            // Electron 앱은 null origin 또는 file:// 프로토콜을 사용할 수 있음
            String[] allowedOrigins = {
                    "http://localhost:5173",
                    "http://localhost:3000",
                    "http://127.0.0.1:5173",
                    "http://127.0.0.1:3000",
                    "null" // Electron 앱의 경우 null origin 허용
            };

            // Origin이 허용된 목록에 있는지 확인
            boolean isAllowed = false;
            if (origin == null || origin.isEmpty()) {
                // Electron 앱이나 같은 출처 요청의 경우 null origin 허용
                isAllowed = true;
            } else {
                for (String allowedOrigin : allowedOrigins) {
                    if (allowedOrigin.equals(origin) || "null".equals(origin)) {
                        isAllowed = true;
                        break;
                    }
                }
            }

            // CORS 헤더 설정
            // Note: allow_credentials와 함께 사용할 때는 "*"를 사용할 수 없으므로
            // 허용된 origin만 설정하거나, credentials 없이 "*"를 사용해야 함
            if (isAllowed) {
                if (origin == null || origin.isEmpty() || "null".equals(origin)) {
                    // Electron 앱이나 같은 출처 요청의 경우 "*" 사용 (credentials 없이)
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                } else {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                }
            } else {
                // 허용되지 않은 origin인 경우 CORS 헤더를 설정하지 않음
                return chain.filter(exchange);
            }
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                    "Origin, Content-Type, Accept, Authorization, X-Requested-With");
            headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Type");
            headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

            // OPTIONS 요청 처리
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }

            return chain.filter(exchange);
        };
    }
}