package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LMS_TRNG_SUMMARY")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
  name = "CourseSummary.withDetails",
  attributeNodes = @NamedAttributeNode("details"))
public class CourseSummary {

  @Id
  @Column(name = "trng_id")
  private Long trainingId;

  @Column(name = "trng_topic")
  private String topics;

  @Column(name = "trng_details")
  private String details;

  @Column(name = "pre_requisites")
  private String prerequisite;

  @Column(name = "level_code")
  private String level;

  @Column(name = "trng_duration")
  private Integer duration;

  @Column(name = "trng_type")
  private String type;

  @Column(name = "rating")
  private String rating;

  @Column(name = "category")
  private String category;

  @Column(name = "tools_needed")
  private String toolsNeeded;

  @Column(name = "course_progress")
  private Long courseProgressPercentage;

  @OneToMany(
    mappedBy = "course",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  @JsonManagedReference("LMS_TRNG_DTLS")
  private List<CourseDetail> lmsTrainingDetails = new ArrayList<>();

  @OneToMany(
    mappedBy = "courseSummary",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  @JsonManagedReference("LMS_USER_TRNG_ENROLLMENT_MAPPING")
  private List<EnrollmentMapping> enrollmentMappings;

  // helpers to keep both sides in sync
  public void addDetail(CourseDetail d) {
    lmsTrainingDetails.add(d);
    d.setCourse(this);
  }
  public void removeDetail(CourseDetail d) {
    lmsTrainingDetails.remove(d);
    d.setCourse(null);
  }
}
