package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lms_trng_dtls_id_generator")
  @SequenceGenerator(name = "lms_trng_dtls_id_generator", sequenceName = "lms_schema.lms_module_id_seq", allocationSize = 1)
  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "module_name", length = 4000)
  private String summary;

  @Column(name = "module_dtls", length = 4000)
  private String details;

  @Column(name = "module_duration")
  private Integer duration;

  @Column(name = "seq_id")
  private Integer seqId;

  @Column(name = "module_path", length = 4000)
  private String trainingLink;

  @Column(name = "CREATED_BY", length = 100)
  private String createdBy;

  @Column(name = "CREATED_TS")
  private OffsetDateTime createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  private OffsetDateTime updatedTs;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trng_id")
  @JsonBackReference("LMS_TRNG_DTLS")
  private CourseSummary course;

  @ManyToOne
  @JoinColumn(name = "trainer_id")
  @JsonBackReference
  private LMSTrainerDetails trainerDetails;

  @OneToMany(mappedBy = "courseDetail", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JsonManagedReference
  private List<EnrollmentDetails> enrollmentDetailsList;

  private Long moduleProgressPercentage;
}
