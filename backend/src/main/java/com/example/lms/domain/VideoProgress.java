package com.example.lms.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "video_progress")
public class VideoProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "lesson_id")
    private String lessonId;

    @Column(name = "current_ts")
    private BigDecimal currentTime;

    @Column(name = "duration")
    private BigDecimal duration;

    @Column(name = "watch_time")
    private BigDecimal watchTime;

    private Boolean completed;

    @Column(name = "last_watched_at")
    private OffsetDateTime lastWatchedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public BigDecimal getCurrentTime() { return currentTime; }
    public void setCurrentTime(BigDecimal currentTime) { this.currentTime = currentTime; }

    public BigDecimal getDuration() { return duration; }
    public void setDuration(BigDecimal duration) { this.duration = duration; }

    public BigDecimal getWatchTime() { return watchTime; }
    public void setWatchTime(BigDecimal watchTime) { this.watchTime = watchTime; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public OffsetDateTime getLastWatchedAt() { return lastWatchedAt; }
    public void setLastWatchedAt(OffsetDateTime lastWatchedAt) { this.lastWatchedAt = lastWatchedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
