package io.github.jiwontechinnovation.analysis.repository;

import io.github.jiwontechinnovation.analysis.document.RoadmapDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB roadmaps 컬렉션 레포지토리
 */
@Repository
public interface RoadmapMongoRepository extends MongoRepository<RoadmapDocument, String> {

    /**
     * 사용자 ID로 로드맵 조회
     */
    List<RoadmapDocument> findByUserId(String userId);

    /**
     * 모든 로드맵 조회
     */
    List<RoadmapDocument> findAll();
}
