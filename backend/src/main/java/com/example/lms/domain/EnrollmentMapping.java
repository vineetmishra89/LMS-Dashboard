package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "LMS_USER_TRNG_ENROLLMENT_MAPPING")
public class EnrollmentMapping {

  @Id
  @Column(name = "trng_enrl_id")
  private String trainingEnrollmentId;

  @Column(name = "email_id")
  private String userId;

  @Column(name = "trng_id")
  private String courseId;

  @Column(name = "status")
  private Integer status;

  @Column(name = "enrolled_ts")
  private OffsetDateTime enrolledTs;

  @Column(name = "started_ts")
  private OffsetDateTime startTs;

  @Column(name = "enrollment_type")
  private OffsetDateTime enrollment_type;

  @OneToMany(mappedBy = "enrollmentMapping", cascade = CascadeType.ALL)
  @JsonManagedReference
  private List<EnrollmentDetails> enrollmentDetailsList;
}
