package com.example.lms.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;
  @Column(columnDefinition = "uuid")
  private UUID userId;
  @Column(columnDefinition = "uuid")
  private UUID courseId;
  private String title;
  private OffsetDateTime issuedAt;
  private String url;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public UUID getCourseId() { return courseId; }
  public void setCourseId(UUID courseId) { this.courseId = courseId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public OffsetDateTime getIssuedAt() { return issuedAt; }
  public void setIssuedAt(OffsetDateTime issuedAt) { this.issuedAt = issuedAt; }
  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }
}
