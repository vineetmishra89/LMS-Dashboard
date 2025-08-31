package com.example.lms.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "training_details")
public class Course {

  @Id
  @Column(name = "training_detail_id")
  private String id;

  @Column(name = "category")
  private String category;

  //@ElementCollection
  //@CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "training_topic")
  private String topics;

  @Column(name = "instructor_name")
  private String instructorName;

  @Column(name = "module_duration")
  private Integer durationMinutes;

  @Column(name = "module_path")
  private String trainingLink;


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
}
