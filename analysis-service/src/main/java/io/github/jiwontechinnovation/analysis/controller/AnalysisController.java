package io.github.jiwontechinnovation.analysis.controller;

import io.github.jiwontechinnovation.analysis.client.UserServiceClient;
import io.github.jiwontechinnovation.analysis.dto.DashboardStatsResponse;
import io.github.jiwontechinnovation.analysis.service.AnalysisService;
import io.github.jiwontechinnovation.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
@Tag(name = "Analysis", description = "분석 통계 API")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final UserServiceClient userServiceClient;

    public AnalysisController(AnalysisService analysisService, UserServiceClient userServiceClient) {
        this.analysisService = analysisService;
        this.userServiceClient = userServiceClient;
    }

    @GetMapping("/stats/debug")
    @Operation(summary = "디버그: 통계 데이터 상세 조회", description = "데이터베이스에서 조회한 원본 데이터를 상세히 확인합니다.")
    public ApiResponse<Object> getDashboardStatsDebug(HttpServletRequest request) {
        UUID userId = null;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())
                    && authentication.getPrincipal() instanceof String) {
                String username = (String) authentication.getPrincipal();
                String authToken = extractToken(request);
                if (authToken != null && !authToken.isEmpty()) {
                    userId = userServiceClient.getUserIdByUsername(username, authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get authentication: " + e.getMessage());
        }

        List<io.github.jiwontechinnovation.analysis.entity.DashboardStat> stats;
        if (userId != null) {
            stats = analysisService.getAllStatsForUser(userId);
        } else {
            stats = analysisService.getAllStatsForUser(null);
        }

        List<Object> debugData = stats.stream()
                .map(stat -> {
                    // 키워드 배열을 Map 리스트로 변환
                    List<Map<String, Object>> keywordsList = stat.getKeywords() != null
                            ? stat.getKeywords().stream()
                                    .map(k -> Map.<String, Object>of(
                                            "category", k.getCategory(),
                                            "value", k.getValue()))
                                    .collect(Collectors.toList())
                            : List.of();

                    return Map.<String, Object>of(
                            "id", stat.getId() != null ? stat.getId() : "null",
                            "userId", stat.getUserId() != null ? stat.getUserId().toString() : "null",
                            "keywords", keywordsList,
                            "createdAt", stat.getCreatedAt() != null ? stat.getCreatedAt().toString() : "null",
                            "updatedAt", stat.getUpdatedAt() != null ? stat.getUpdatedAt().toString() : "null");
                })
                .collect(Collectors.toList());

        return ApiResponse.success("디버그 데이터",
                Map.of("userId", userId != null ? userId.toString() : "null", "stats", debugData));
    }

    @GetMapping("/stats")
    @Operation(summary = "대시보드 통계 조회", description = "레이더 차트에 표시할 통계 데이터와 스트릭 정보를 조회합니다.")
    public ApiResponse<DashboardStatsResponse> getDashboardStats(
            HttpServletRequest request,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year) {
        UUID userId = null;

        try {
            // SecurityContext에서 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())
                    && authentication.getPrincipal() instanceof String) {
                String username = (String) authentication.getPrincipal();
                String authToken = extractToken(request);
                if (authToken != null && !authToken.isEmpty()) {
                    userId = userServiceClient.getUserIdByUsername(username, authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to get authentication: " + e.getMessage());
        }

        try {
            DashboardStatsResponse response = analysisService.getDashboardStats(userId, year);
            return ApiResponse.success("통계 데이터 조회 성공", response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in getDashboardStats: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/stats/keywords")
    @Operation(summary = "키워드 통계 저장", description = "로드맵에서 추출한 키워드를 통계에 반영합니다.")
    @org.springframework.web.bind.annotation.RequestMapping(produces = "application/json")
    public ApiResponse<Void> saveKeywords(
            @RequestBody Map<String, Object> request) {
        try {
            String userIdStr = (String) request.get("userId");
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) request.get("keywords");

            UUID userId = null;
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = UUID.fromString(userIdStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid userId format: " + userIdStr);
                }
            }

            if (keywords != null && !keywords.isEmpty()) {
                analysisService.saveKeywords(userId, keywords);
            }

            return ApiResponse.success("키워드 저장 성공", null);
        } catch (Exception e) {
            System.err.println("키워드 저장 오류: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("키워드 저장 실패: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
