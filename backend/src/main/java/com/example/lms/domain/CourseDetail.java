package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "course_detail")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseDetail {

  @Id
  @Column(name = "training_detail_id")
  private String trainingDetailId;

  @Column(name = "training_id")
  private String trainingId;

  @Column(name = "thumbnail_url")
  private String thumbnail;

  @Column(name = "created_at")
  private Date createdAt;

  @Column(name = "updated_at")
  private Date updatedAt;

  @Column(name = "module_duration")
  private Integer duration;

  @Column(name = "instructor_name")
  private String instructorName;

  @Column(name = "module_path")
  @JsonProperty("videoUrl")
  private String trainingLink;

  @Column(name = "trainers_current_feedback")
  private String trainersCurrentFeedback;

  @Column(name = "current_user_feedback")
  private String currentUserFeedback;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id")
  @JsonBackReference("course-details")
  private CourseMaster course;
}
