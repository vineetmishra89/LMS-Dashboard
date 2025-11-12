package com.example.lms.domain;

import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TrainingSearchHistoryId implements Serializable {

  private Integer trngId;
  private String emailId;
  private OffsetDateTime viewTs;
}
