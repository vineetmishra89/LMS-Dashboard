package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "LMS_USER_TRNG_ENROLLMENT_DTLS")
public class EnrollmentDetails {

  @Column(name = "status")
  private String status;

  @Column(name = "current_learning_ts")
  private Integer currentLearningTs;

  @ManyToOne
  @JoinColumn(name = "trng_enrl_id")
  @JsonBackReference
  private EnrollmentMapping enrollmentMapping;

  @ManyToOne
  @JoinColumn(name = "module_id")
  @JsonBackReference
  private CourseDetail courseDetail;

}
