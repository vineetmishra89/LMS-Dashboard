package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LMS_USER_TRNG_ENROLLMENT_MAPPING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lms_trng_enrl_id_generator")
  @SequenceGenerator(name = "lms_trng_enrl_id_generator", sequenceName = "lms_schema.lms_trng_enrl_id_seq", allocationSize = 1)
  @Column(name = "trng_enrl_id")
  private Long trainingEnrollmentId;

  @Column(name = "email_id")
  private String userId;

  @Column(name = "status")
  private String status;

  @Column(name = "enrolled_ts")
  private OffsetDateTime enrolledTs;

  @Column(name = "enrolled_by_email_id")
  private String enrolledByEmailId;

  @Column(name = "started_ts")
  private OffsetDateTime startTs;

  @Column(name = "enrollment_type")
  private String enrollmentType;

  @Column(name = "progress_percent")
  private Long progressPercent;

  @Column(name = "created_ts")
  private OffsetDateTime createdTs;

  @Column(name = "updated_ts")
  private OffsetDateTime updatedTs;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_By")
  private String updatedBy;

  @OneToMany(mappedBy = "enrollmentMapping", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JsonManagedReference
  private List<EnrollmentDetails> enrollmentDetailsList = new ArrayList<>();

  @ManyToOne(fetch=FetchType.EAGER)
  @JoinColumn(name="trng_id")
  @JsonBackReference
  private CourseSummary courseSummary;

  public void addEnrollmentDetail(EnrollmentDetails enrollmentDetails){
    if(this.enrollmentDetailsList == null){
      this.enrollmentDetailsList = new ArrayList<>();
    }
    enrollmentDetails.setEnrollmentMapping(this);
    this.enrollmentDetailsList.add(enrollmentDetails);

  }

  public void removeEnrollmentDetail(EnrollmentDetails enrollmentDetails){
    this.enrollmentDetailsList.remove(enrollmentDetails);
    enrollmentDetails.setEnrollmentMapping(null);

  }
}
