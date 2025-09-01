package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.Date;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "training_details")
public class Course {

  @Id
  @Column(name = "training_detail_id")
  private String id;

  @Column(name = "training_name")
  private String title;

  @Column(name = "category")
  private String category;

  //@ElementCollection
  //@CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "training_topic")
  private String topics;

  @Column(name = "thumbnail_url")
  private String thumbnail;

  @Column(name = "created_at")
  private Date createdAt;

  @Column(name = "updated_at")
  private Date updatedAt;

  @Column(name = "instructor_name")
  private String instructorName;

  @Column(name = "module_duration")
  private Integer durationMinutes;

  @Column(name = "module_path")
  @JsonProperty("videoUrl")
  private String trainingLink;


  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getTopics() { return topics; }
  public void setTopics(String topics) { this.topics = topics; }
  public String getInstructorName() { return instructorName; }
  public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public String getTrainingLink() {
    return trainingLink;
  }

  public void setTrainingLink(String trainingLink) {
    this.trainingLink = trainingLink;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }
}
