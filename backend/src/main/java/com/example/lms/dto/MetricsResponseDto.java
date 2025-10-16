package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponseDto {
  private List<TopTraineeDto> topTrainees;
  private List<TopCourseDto> topRatedCourses;
  private List<TopCourseDto> topEnrolledCourses;
  private List<TopCourseDto> topViewedCourses;
  private List<TopCourseDto> topCompletedCourses;
  private List<UserTrainingDumpDto> userTrainingDump;
}
