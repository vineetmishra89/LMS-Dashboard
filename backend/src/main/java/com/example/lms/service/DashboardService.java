package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;

import java.util.List;

public interface DashboardService {

  DashboardStatsSummary getDashboardSummary(String userId);

  List<CourseSummary> getEnrolledCourses(String userId);

  List<CourseSummary>  getPendingCourses(String userId);

  List<CourseSummary> getCompletedCourses(String userId);

  List<CourseSummary> getLikedCourses(String userId);
}
