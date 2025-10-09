package com.example.lms.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsSummary {

  private long completedCourse;
  private long enrolledCourse;
  private long pendingCourse;
  private long hoursSpent;
  private long hoursArchived;
  private long likedCourses;

}
