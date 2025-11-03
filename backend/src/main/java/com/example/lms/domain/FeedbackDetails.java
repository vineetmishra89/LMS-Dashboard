package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_USER_TRNG_FEEDBACK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDetails {

  @Id
  @Column(name = "feedback_id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_id_generator")
  @SequenceGenerator(name = "feedback_id_generator", sequenceName = "lms_schema.lms_trng_feedback_id_seq", allocationSize = 1)
  @JsonIgnore
  private Long feedbackId;

  @Column(name = "trng_enrl_id")
  private Long trainingEnrollmentId;

  @Column(name = "EMAIL_ID", nullable = false, length = 100)
  private String emailId;

  @Column(name = "feedback_ques")
  private String feedbackQuestion;

  @Column(name = "feedback_rating")
  private int feedbackRating;

  @Column(name = "CREATED_BY", length = 100)
  @JsonIgnore
  private String createdBy;

  @Column(name = "CREATED_TS")
  @JsonIgnore
  private OffsetDateTime createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  @JsonIgnore
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  @JsonIgnore
  private OffsetDateTime updatedTs;
}
