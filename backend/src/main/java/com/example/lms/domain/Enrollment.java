package com.example.lms.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class Enrollment {
  @Id
  private String id;
  private String userId;
  private String courseId;
  private Integer progressPercent;
  private OffsetDateTime lastAccessedAt;
  private String status;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getCourseId() { return courseId; }
  public void setCourseId(String courseId) { this.courseId = courseId; }
  public Integer getProgressPercent() { return progressPercent; }
  public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
  public OffsetDateTime getLastAccessedAt() { return lastAccessedAt; }
  public void setLastAccessedAt(OffsetDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
