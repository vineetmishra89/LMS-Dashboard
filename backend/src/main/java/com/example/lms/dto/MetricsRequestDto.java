package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricsRequestDto {
  private String category;
  private String level;
  private String technology;
  private TimePeriod timePeriod;
  private Integer topN;
}
