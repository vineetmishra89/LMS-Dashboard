package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "LMS_USER_TRNG_ENROLLMENT_MAPPING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentMapping {

  @Id
  @Column(name = "trng_enrl_id")
  private Long trainingEnrollmentId;

  @Column(name = "email_id")
  private String userId;

  @Column(name = "status")
  private String status;

  @Column(name = "enrolled_ts")
  private OffsetDateTime enrolledTs;

  @Column(name = "started_ts")
  private OffsetDateTime startTs;

  @Column(name = "enrollment_type")
  private String enrollment_type;

  @Column(name = "progress_percent")
  private Long progressPercent;

  @Column(name = "last_accessed_ts")
  private OffsetDateTime lastAccessedAt;

  @OneToMany(mappedBy = "enrollmentMapping", cascade = CascadeType.ALL)
  @JsonManagedReference
  private List<EnrollmentDetails> enrollmentDetailsList;

  @ManyToOne(fetch=FetchType.EAGER)
  @JoinColumn(name="trng_id")
  @JsonManagedReference
  private CourseSummary courseSummary;
}
