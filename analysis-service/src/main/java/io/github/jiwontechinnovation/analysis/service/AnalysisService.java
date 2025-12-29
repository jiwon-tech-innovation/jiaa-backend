package io.github.jiwontechinnovation.analysis.service;

import io.github.jiwontechinnovation.analysis.document.RoadmapDocument;
import io.github.jiwontechinnovation.analysis.dto.DashboardStatsResponse;
import io.github.jiwontechinnovation.analysis.dto.RadarStatDto;
import io.github.jiwontechinnovation.analysis.entity.DashboardStat;
import io.github.jiwontechinnovation.analysis.repository.DashboardStatRepository;
import io.github.jiwontechinnovation.analysis.repository.RoadmapMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
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
    private final MongoTemplate mongoTemplate;

    public AnalysisService(DashboardStatRepository dashboardStatRepository,
            RoadmapMongoRepository roadmapMongoRepository,
            MongoTemplate mongoTemplate) {
        this.dashboardStatRepository = dashboardStatRepository;
        this.roadmapMongoRepository = roadmapMongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardStatsResponse getDashboardStats(UUID userId, Integer year) {
        // 레이더 차트 데이터 - 배열 구조에서 키워드 추출
        DashboardStat stat = null;
        if (userId != null) {
            stat = dashboardStatRepository.findFirstByUserId(userId).orElse(null);
        } else {
            stat = dashboardStatRepository.findFirstByUserIdIsNull().orElse(null);
        }

        List<RadarStatDto> radarData = new ArrayList<>();
        if (stat != null && stat.getKeywords() != null) {
            radarData = stat.getKeywords().stream()
                    .sorted(Comparator.comparing(DashboardStat.KeywordStat::getValue).reversed())
                    .limit(6)
                    .map(keywordStat -> new RadarStatDto(keywordStat.getCategory(), keywordStat.getValue()))
                    .collect(Collectors.toList());
        }

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
        int totalDays = 0; // 전체 로드맵 일수

        for (RoadmapDocument roadmap : roadmaps) {
            if (roadmap.getItems() == null)
                continue;

            // 로드맵의 전체 일수 계산 (items의 개수)
            totalDays += roadmap.getItems().size();

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

        // 오늘부터 역순으로 연속된 날짜 체크
        // 오늘 완료했으면 오늘부터, 오늘 완료 안 했으면 어제부터 체크
        if (!completedDates.contains(today)) {
            checkDate = today.minusDays(1);
        }

        // 연속된 날짜가 있는 동안 스트릭 증가
        while (completedDates.contains(checkDate)) {
            currentStreak++;
            checkDate = checkDate.minusDays(1);
            
            // 무한 루프 방지 (너무 오래된 날짜까지 체크하지 않도록)
            if (checkDate.isBefore(today.minusYears(1))) {
                break;
            }
        }

        // 컨트리뷰션 데이터 생성
        int targetYear = year != null ? year : today.getYear();
        List<List<Integer>> contributionData = generateContributionData(completedDates, targetYear);

        return new ActivityData(currentStreak, completedDates.size(), totalDays, completedItems, contributionData);
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

    public List<DashboardStat> getAllStatsForUser(UUID userId) {
        if (userId != null) {
            return dashboardStatRepository.findByUserId(userId);
        } else {
            return dashboardStatRepository.findByUserIdIsNull();
        }
    }

    /**
     * 키워드 리스트를 받아서 각 키워드의 통계를 원자적으로 증가시킵니다.
     * MongoDB 배열 업데이트 연산자를 사용하여 Race Condition을 방지합니다.
     */
    @Transactional
    public void saveKeywords(UUID userId, List<String> keywords) {
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String trimmedKeyword = keyword.trim();
                incrementKeywordValue(userId, trimmedKeyword);
            }
        }
    }

    /**
     * MongoDB 배열 업데이트를 사용하여 키워드 값을 원자적으로 증가시킵니다.
     * MongoDB Java Driver를 직접 사용하여 arrayFilters를 활용한 원자적 업데이트를 보장합니다.
     */
    private void incrementKeywordValue(UUID userId, String keyword) {
        MongoCollection<Document> collection = mongoTemplate.getCollection("dashboard_stats");
        
        // 쿼리 조건
        Bson queryFilter;
        if (userId != null) {
            queryFilter = Filters.eq("userId", userId.toString());
        } else {
            queryFilter = Filters.eq("userId", null);
        }

        // 배열 필터: category가 일치하는 키워드 찾기
        List<Bson> arrayFilters = Arrays.asList(
            Filters.eq("elem.category", keyword)
        );

        // 업데이트: 키워드가 있으면 값 증가, 없으면 추가
        Bson update = com.mongodb.client.model.Updates.combine(
            com.mongodb.client.model.Updates.inc("keywords.$[elem].value", 1),
            com.mongodb.client.model.Updates.set("updatedAt", LocalDateTime.now())
        );

        // UpdateOptions 설정
        UpdateOptions options = new UpdateOptions();
        options.arrayFilters(arrayFilters);
        options.upsert(true);

        // 원자적 업데이트 시도 (키워드가 있는 경우)
        long modifiedCount = collection.updateOne(queryFilter, update, options).getModifiedCount();

        // 업데이트가 안 되었다면 (키워드가 배열에 없었거나 문서가 없었던 경우)
        if (modifiedCount == 0) {
            // 문서가 존재하는지 확인
            boolean documentExists = collection.countDocuments(queryFilter) > 0;
            
            if (documentExists) {
                // 문서는 있지만 키워드가 없는 경우 - 배열에 추가
                Bson addUpdate = com.mongodb.client.model.Updates.combine(
                    com.mongodb.client.model.Updates.push("keywords", 
                        new Document("category", keyword).append("value", 1)),
                    com.mongodb.client.model.Updates.set("updatedAt", LocalDateTime.now())
                );
                collection.updateOne(queryFilter, addUpdate);
            } else {
                // 문서 자체가 없는 경우 - 새로 생성
                Document newDoc = new Document();
                if (userId != null) {
                    newDoc.append("userId", userId.toString());
                } else {
                    newDoc.append("userId", null);
                }
                newDoc.append("keywords", Arrays.asList(
                    new Document("category", keyword).append("value", 1)
                ));
                newDoc.append("createdAt", LocalDateTime.now());
                newDoc.append("updatedAt", LocalDateTime.now());
                collection.insertOne(newDoc);
            }
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
