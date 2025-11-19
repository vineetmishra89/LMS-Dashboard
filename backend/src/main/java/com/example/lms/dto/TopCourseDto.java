package com.example.lms.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopCourseDto {
  private Long trainingId;
  private String topic;
  private String category;
  private String level;
  private BigDecimal rating;
  private Long enrollmentCount;
  private Long completedCount;
  private Long viewCount;
}
