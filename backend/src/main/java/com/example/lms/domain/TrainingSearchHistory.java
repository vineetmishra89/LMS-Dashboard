package com.example.lms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_TRNG_SEARCH_HIST", schema = "lms_schema")
@IdClass(TrainingSearchHistoryId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSearchHistory {

  @Id
  @Column(name = "TRNG_ID", nullable = false)
  private Integer trngId;

  @Id
  @Column(name = "EMAIL_ID", nullable = false, length = 100)
  private String emailId;

  @Id
  @Column(name = "VIEW_TS", nullable = false)
  private OffsetDateTime viewTs;

  @Column(name = "CREATED_BY", length = 100)
  private String createdBy;

  @Column(name = "CREATED_TS")
  private OffsetDateTime createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  private OffsetDateTime updatedTs;
}
