package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTrainingDumpDto {
  private String userId;
  private Long trainingId;
  private String trainingTopic;
  private String category;
  private String level;
  private String status;
  private Long progressPercent;
  private OffsetDateTime enrolledTs;
  private OffsetDateTime startedTs;
  private OffsetDateTime lastAccessedTs;
}
