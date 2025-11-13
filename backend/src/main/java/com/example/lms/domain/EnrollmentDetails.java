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

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lms_trng_enrl_dtl_id_generator")
  @SequenceGenerator(name = "lms_trng_enrl_dtl_id_generator", sequenceName = "lms_schema.trng_enrl_dtl_id_seq", allocationSize = 1)
  @Column(name = "TRNG_ENRL_DTL_ID")
  private Long enrollmentDetailsId;

  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "status")
  private String status;

  @Column(name = "created_ts")
  private OffsetDateTime createdTs;

  @Column(name = "updated_ts")
  private OffsetDateTime updatedTs;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_By")
  private String updatedBy;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "trng_enrl_id", nullable = false)
  @JsonBackReference
  private EnrollmentMapping enrollmentMapping;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("moduleId")
  @JoinColumn(name = "module_id")
  @JsonBackReference
  private CourseDetail courseDetail;

}
