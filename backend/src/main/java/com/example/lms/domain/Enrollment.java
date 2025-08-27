package com.example.lms.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;
  @Column(columnDefinition = "uuid")
  private UUID userId;
  @Column(columnDefinition = "uuid")
  private UUID courseId;
  private Integer progressPercent;
  private OffsetDateTime lastAccessedAt;
  private String status;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public UUID getCourseId() { return courseId; }
  public void setCourseId(UUID courseId) { this.courseId = courseId; }
  public Integer getProgressPercent() { return progressPercent; }
  public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
  public OffsetDateTime getLastAccessedAt() { return lastAccessedAt; }
  public void setLastAccessedAt(OffsetDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
