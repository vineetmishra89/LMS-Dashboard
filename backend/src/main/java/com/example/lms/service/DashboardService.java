package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;
import com.example.lms.domain.DashboardSummary;
import com.example.lms.domain.EnrollmentMapping;

import java.util.List;

public interface DashboardService {

  DashboardStatsSummary getDashboardSummary(String userId);

  List<DashboardSummary> getEnrolledCourses(String userId);

  List<DashboardSummary>  getPendingCourses(String userId);

  List<DashboardSummary> getCompletedCourses(String userId);

  List<CourseSummary> getLikedCourses(String userId);
}
