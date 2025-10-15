package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopTraineeDto {
  private String userId;
  private Long completedCourses;
  private Double averageProgress;
  private Long totalEnrollments;
}
