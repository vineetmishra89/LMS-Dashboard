package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lms_trng_summary_id_generator")
  @SequenceGenerator(name = "lms_trng_summary_id_generator", sequenceName = "lms_schema.lms_trng_id_seq", allocationSize = 1)
  @Column(name = "trng_id")
  private Long trainingId;

  @Column(name = "trng_topic", length = 4000)
  private String topics;

  @Column(name = "trng_details", length = 4000)
  private String details;

  @Column(name = "pre_requisites", length = 4000)
  private String prerequisite;

  @Column(name = "level_code")
  private String level;

  @Column(name = "trng_duration")
  private Integer duration;

  @Column(name = "trng_type")
  private String type;

  @Column(name = "rating")
  private Integer rating;

  @Column(name = "category")
  private String category;

  @Column(name = "trng_skill_area")
  private String trainingSkillArea;

  @Column(name = "tools_needed", length = 4000)
  private String toolsNeeded;

  @Column(name = "folder_path", length = 4000)
  private String folderPath;

  @Column(name = "course_progress")
  private Long courseProgressPercentage;

  @Column(name = "trainer_emails")
  private String trainerEmailIds;

  @Column(name = "CREATED_BY", length = 100)
  private String createdBy;

  @Column(name = "CREATED_TS")
  private OffsetDateTime createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  private OffsetDateTime updatedTs;

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
  private List<EnrollmentMapping> enrollmentMappings = new ArrayList<>();

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
