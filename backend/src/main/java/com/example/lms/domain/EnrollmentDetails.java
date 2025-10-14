package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_USER_TRNG_ENROLLMENT_DTLS")
@Getter
@Setter
@NoArgsConstructor
public class EnrollmentDetails implements Serializable {

  @EmbeddedId
  private EnrollmentDetailsId enrollmentDetailsId;

  @Column(name = "status")
  private String status;

  @Column(name = "current_learning_ts")
  private Integer currentLearningTs;

  @Column(name = "last_accessed_ts")
  private OffsetDateTime lastAccessedAt;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("trainingEmrollmentId")
  @JoinColumn(name = "trng_enrl_id")
  @JsonBackReference
  private EnrollmentMapping enrollmentMapping;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("moduleId")
  @JoinColumn(name = "module_id")
  @JsonBackReference
  private CourseDetail courseDetail;

}
