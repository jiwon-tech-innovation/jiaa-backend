package io.github.jiwontechinnovation.analysis.repository;

import io.github.jiwontechinnovation.analysis.entity.DashboardStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardStatRepository extends JpaRepository<DashboardStat, UUID> {
    List<DashboardStat> findByUserId(UUID userId);
    
    Optional<DashboardStat> findByUserIdAndCategory(UUID userId, String category);
    
    // 전체 통계 조회 (userId가 null인 경우)
    List<DashboardStat> findByUserIdIsNull();
    
    // userId가 null이고 category로 조회
    Optional<DashboardStat> findByUserIdIsNullAndCategory(String category);
}

