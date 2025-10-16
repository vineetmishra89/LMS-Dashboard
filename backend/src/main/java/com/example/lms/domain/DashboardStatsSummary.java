package com.example.lms.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsSummary {

  private long completedCourse;
  private long enrolledCourse;
  private long pendingCourse;
  private long hoursSpent;
  private long hoursArchived;
  private long likedCourses;

}
