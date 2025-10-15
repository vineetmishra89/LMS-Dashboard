package com.example.lms.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
  private Long trngId;
  private String catgory;
  private String details;
  private String level;
  private long duration;
  private String instructorName;
  private String description;
  private String trainingName;
  private long progress;
}
