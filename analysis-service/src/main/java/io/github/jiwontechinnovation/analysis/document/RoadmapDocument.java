package io.github.jiwontechinnovation.analysis.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB roadmaps 컬렉션 문서
 */
@Document(collection = "roadmaps")
public class RoadmapDocument {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("session_id")
    private String sessionId;

    private String name;

    private List<RoadmapItem> items;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoadmapItem> getItems() {
        return items;
    }

    public void setItems(List<RoadmapItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 로드맵 항목 (embedded document)
     */
    public static class RoadmapItem {
        private Integer day;
        private String content;
        private String time;
        private List<Task> tasks;

        @Field("is_completed")
        private Integer isCompleted;

        @Field("completed_at")
        private LocalDateTime completedAt;

        @Field("created_at")
        private LocalDateTime createdAt;

        // Getters and Setters
        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }

        public Integer getIsCompleted() {
            return isCompleted;
        }

        public void setIsCompleted(Integer isCompleted) {
            this.isCompleted = isCompleted;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 과업 (새 구조에서 tasks 배열)
     */
    public static class Task {
        private Integer rank;
        private String content;
        private String time;

        @Field("is_completed")
        private Integer isCompleted;

        @Field("completed_at")
        private LocalDateTime completedAt;

        private String details;

        // Getters and Setters
        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Integer getIsCompleted() {
            return isCompleted;
        }

        public void setIsCompleted(Integer isCompleted) {
            this.isCompleted = isCompleted;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}
