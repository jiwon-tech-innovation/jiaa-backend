package io.github.jiwontechinnovation.analysis.service;

import io.github.jiwontechinnovation.analysis.document.RoadmapDocument;
import io.github.jiwontechinnovation.analysis.dto.DashboardStatsResponse;
import io.github.jiwontechinnovation.analysis.dto.RadarStatDto;
import io.github.jiwontechinnovation.analysis.entity.DashboardStat;
import io.github.jiwontechinnovation.analysis.repository.DashboardStatRepository;
import io.github.jiwontechinnovation.analysis.repository.RoadmapMongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalysisService {
    private final DashboardStatRepository dashboardStatRepository;
    private final RoadmapMongoRepository roadmapMongoRepository;

    public AnalysisService(DashboardStatRepository dashboardStatRepository,
            RoadmapMongoRepository roadmapMongoRepository) {
        this.dashboardStatRepository = dashboardStatRepository;
        this.roadmapMongoRepository = roadmapMongoRepository;
    }

    public DashboardStatsResponse getDashboardStats(UUID userId, Integer year) {
        // 레이더 차트 데이터
        List<DashboardStat> stats;
        if (userId != null) {
            stats = dashboardStatRepository.findByUserId(userId);
        } else {
            stats = dashboardStatRepository.findByUserIdIsNull();
        }

        List<RadarStatDto> radarData = stats.stream()
                .sorted(Comparator.comparing(DashboardStat::getValue).reversed())
                .limit(6)
                .map(stat -> new RadarStatDto(stat.getCategory(), stat.getValue()))
                .collect(Collectors.toList());

        // 스트릭 및 활동 데이터 계산
        ActivityData activityData = calculateActivityData(userId != null ? userId.toString() : null, year);

        return new DashboardStatsResponse(
                radarData,
                activityData.currentStreak,
                activityData.completedDays,
                activityData.totalDays,
                activityData.completedItems,
                activityData.contributionData);
    }

    /**
     * 활동 데이터 계산 (스트릭, 완료일수, 컨트리뷰션 그래프)
     */
    private ActivityData calculateActivityData(String userId, Integer year) {
        List<RoadmapDocument> roadmaps = userId != null
                ? roadmapMongoRepository.findByUserId(userId)
                : roadmapMongoRepository.findAll();

        Set<LocalDate> completedDates = new HashSet<>();
        int totalItems = 0;
        int completedItems = 0;

        for (RoadmapDocument roadmap : roadmaps) {
            if (roadmap.getItems() == null)
                continue;

            for (RoadmapDocument.RoadmapItem item : roadmap.getItems()) {
                // 새 구조 (tasks 배열)
                if (item.getTasks() != null && !item.getTasks().isEmpty()) {
                    for (RoadmapDocument.Task task : item.getTasks()) {
                        totalItems++;
                        if (task.getIsCompleted() != null && task.getIsCompleted() == 1) {
                            completedItems++;
                            if (task.getCompletedAt() != null) {
                                completedDates.add(task.getCompletedAt().toLocalDate());
                            }
                        }
                    }
                } else {
                    // 레거시 구조
                    totalItems++;
                    if (item.getIsCompleted() != null && item.getIsCompleted() == 1) {
                        completedItems++;
                        if (item.getCompletedAt() != null) {
                            completedDates.add(item.getCompletedAt().toLocalDate());
                        }
                    }
                }
            }
        }

        // 스트릭 계산 (오늘부터 역순으로 연속된 날짜 계산)
        int currentStreak = 0;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;

        // 오늘 완료 안 했으면 어제부터 체크
        if (!completedDates.contains(checkDate)) {
            checkDate = today.minusDays(1);
        }

        while (completedDates.contains(checkDate)) {
            currentStreak++;
            checkDate = checkDate.minusDays(1);
        }

        // 컨트리뷰션 데이터 생성
        int targetYear = year != null ? year : today.getYear();
        List<List<Integer>> contributionData = generateContributionData(completedDates, targetYear);

        return new ActivityData(currentStreak, completedDates.size(), totalItems, completedItems, contributionData);
    }

    /**
     * 컨트리뷰션 그래프 데이터 생성
     */
    private List<List<Integer>> generateContributionData(Set<LocalDate> completedDates, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // 시작 요일 (일요일 = 0)
        int startDayOfWeek = startDate.getDayOfWeek().getValue() % 7;

        List<List<Integer>> data = new ArrayList<>();
        List<Integer> currentWeek = new ArrayList<>();

        // 첫 주 패딩
        for (int i = 0; i < startDayOfWeek; i++) {
            currentWeek.add(-1);
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int dayOffset = 0; dayOffset < totalDays; dayOffset++) {
            LocalDate currentDate = startDate.plusDays(dayOffset);

            int level = completedDates.contains(currentDate) ? 3 : 0;
            currentWeek.add(level);

            if (currentWeek.size() == 7) {
                data.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }

        // 마지막 주 패딩
        if (!currentWeek.isEmpty()) {
            while (currentWeek.size() < 7) {
                currentWeek.add(-1);
            }
            data.add(currentWeek);
        }

        return data;
    }

    @Transactional
    public void saveOrUpdateStat(UUID userId, String category, Integer value) {
        if (userId != null) {
            dashboardStatRepository.findByUserIdAndCategory(userId, category)
                    .ifPresentOrElse(
                            stat -> {
                                stat.setValue(value);
                                stat.setUpdatedAt(LocalDateTime.now());
                                dashboardStatRepository.save(stat);
                            },
                            () -> dashboardStatRepository.save(new DashboardStat(userId, category, value)));
        } else {
            dashboardStatRepository.findByUserIdIsNullAndCategory(category)
                    .ifPresentOrElse(
                            stat -> {
                                stat.setValue(value);
                                stat.setUpdatedAt(LocalDateTime.now());
                                dashboardStatRepository.save(stat);
                            },
                            () -> dashboardStatRepository.save(new DashboardStat(null, category, value)));
        }
    }

    public List<DashboardStat> getAllStatsForUser(UUID userId) {
        if (userId != null) {
            return dashboardStatRepository.findByUserId(userId);
        } else {
            return dashboardStatRepository.findByUserIdIsNull();
        }
    }

    /**
     * 활동 데이터 내부 클래스
     */
    private record ActivityData(
            int currentStreak,
            int completedDays,
            int totalDays,
            int completedItems,
            List<List<Integer>> contributionData) {
    }
}
