package com.example.lms.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "courses")
public class Course {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;
  private String title;
  private String category;
  @ElementCollection
  @CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "topic")
  private List<String> topics = new ArrayList<>();
  private String instructorName;
  private Integer durationMinutes;
  private String thumbnail;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public List<String> getTopics() { return topics; }
  public void setTopics(List<String> topics) { this.topics = topics; }
  public String getInstructorName() { return instructorName; }
  public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public String getThumbnail() { return thumbnail; }
  public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
