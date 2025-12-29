package io.github.jiwontechinnovation.analysis.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "dashboard_stats")
public class DashboardStat {
    @Id
    private String id;

    @Indexed
    private UUID userId;

    private List<KeywordStat> keywords;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected DashboardStat() {
        this.keywords = new ArrayList<>();
    }

    public DashboardStat(UUID userId) {
        this.userId = userId;
        this.keywords = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<KeywordStat> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeywordStat> keywords) {
        this.keywords = keywords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 키워드 통계 내부 클래스
     */
    public static class KeywordStat {
        private String category;
        private Integer value;

        public KeywordStat() {
        }

        public KeywordStat(String category, Integer value) {
            this.category = category;
            this.value = value;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}

