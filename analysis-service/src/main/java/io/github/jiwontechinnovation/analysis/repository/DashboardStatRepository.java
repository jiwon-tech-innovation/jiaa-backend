package io.github.jiwontechinnovation.analysis.repository;

import io.github.jiwontechinnovation.analysis.entity.DashboardStat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardStatRepository extends MongoRepository<DashboardStat, String> {
    List<DashboardStat> findByUserId(UUID userId);
    
    // 전체 통계 조회 (userId가 null인 경우)
    List<DashboardStat> findByUserIdIsNull();
    
    // userId로 단일 문서 조회 (배열 구조에서는 userId당 하나의 문서)
    Optional<DashboardStat> findFirstByUserId(UUID userId);
    
    Optional<DashboardStat> findFirstByUserIdIsNull();
}

