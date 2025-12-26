package io.github.jiwontechinnovation.analysis.service;

import io.github.jiwontechinnovation.analysis.dto.DashboardStatsResponse;
import io.github.jiwontechinnovation.analysis.dto.RadarStatDto;
import io.github.jiwontechinnovation.analysis.entity.DashboardStat;
import io.github.jiwontechinnovation.analysis.repository.DashboardStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalysisService {
    private final DashboardStatRepository dashboardStatRepository;

    public AnalysisService(DashboardStatRepository dashboardStatRepository) {
        this.dashboardStatRepository = dashboardStatRepository;
    }

    public DashboardStatsResponse getDashboardStats(UUID userId) {
        List<DashboardStat> stats;
        
        if (userId != null) {
            stats = dashboardStatRepository.findByUserId(userId);
        } else {
            // userId가 null이면 전체 통계 조회
            stats = dashboardStatRepository.findByUserIdIsNull();
        }
        
        // 데이터베이스에서 조회한 값을 그대로 반환
        // value 값이 높은 순서로 정렬하고 상위 6개만 선택
        // 디버깅을 위해 로그 출력
        System.out.println("=== Dashboard Stats for userId: " + userId + " ===");
        stats.forEach(stat -> System.out.println("Category: " + stat.getCategory() + ", Value: " + stat.getValue()));
        
        List<RadarStatDto> radarData = stats.stream()
                .sorted(Comparator.comparing(DashboardStat::getValue).reversed()) // value 내림차순 정렬
                .limit(6) // 상위 6개만 선택
                .map(stat -> new RadarStatDto(stat.getCategory(), stat.getValue()))
                .collect(Collectors.toList());
        
        return new DashboardStatsResponse(radarData);
    }

    @Transactional
    public void saveOrUpdateStat(UUID userId, String category, Integer value) {
        if (userId != null) {
            dashboardStatRepository.findByUserIdAndCategory(userId, category)
                    .ifPresentOrElse(
                            stat -> {
                                stat.setValue(value);
                                dashboardStatRepository.save(stat);
                            },
                            () -> dashboardStatRepository.save(new DashboardStat(userId, category, value))
                    );
        } else {
            // 전체 통계 업데이트
            dashboardStatRepository.findByUserIdIsNullAndCategory(category)
                    .ifPresentOrElse(
                            stat -> {
                                stat.setValue(value);
                                dashboardStatRepository.save(stat);
                            },
                            () -> dashboardStatRepository.save(new DashboardStat(null, category, value))
                    );
        }
    }

    public List<DashboardStat> getAllStatsForUser(UUID userId) {
        if (userId != null) {
            return dashboardStatRepository.findByUserId(userId);
        } else {
            return dashboardStatRepository.findByUserIdIsNull();
        }
    }
}

