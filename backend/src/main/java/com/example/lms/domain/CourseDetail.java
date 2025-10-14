package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "LMS_TRNG_DTLS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CourseDetail {

  @Id
  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "module_summary")
  private String summary;

  @Column(name = "module_dtls")
  private String details;

  @Column(name = "module_duration")
  private Integer duration;

  @Column(name = "module_path")
  private String trainingLink;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trng_id")
  @JsonBackReference("LMS_TRNG_DTLS")
  private CourseSummary course;

  @ManyToOne
  @JoinColumn(name = "trainer_id")
  @JsonBackReference
  private LMSTrainerDetails trainerDetails;

  @OneToMany(mappedBy = "courseDetail", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<EnrollmentDetails> enrollmentDetailsList;

  private long moduleProgressPercentage;
}
