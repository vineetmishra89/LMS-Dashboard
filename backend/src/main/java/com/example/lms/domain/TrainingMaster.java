package com.example.lms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "course_master")
@Getter
@Setter
public class TrainingMaster {

  @Id
  @Column(name = "id")
  private String trainingId;

  @Column(name = "training_name")
  private String trainingName;

  @Column(name = "category")
  private String category;

  //@ElementCollection
  //@CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "training_topic")
  private String topics;

  @Column(name = "description")
  private String description;

  @Column(name = "content")
  private String content;

  @Column(name = "instructor_name")
  private String instructorName;

  @Column(name = "duration")
  private Integer duration;

  @Column(name = "review_comments")
  private String reviewComments;

  @Column(name = "prerequisite")
  private String prerequisite;

  @Column(name = "level")
  private String level;

  @Column(name = "tools_needed")
  private String toolsNeeded;
}
