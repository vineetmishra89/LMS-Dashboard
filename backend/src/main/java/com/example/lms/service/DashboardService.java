package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;
import com.example.lms.domain.EnrollmentMapping;

import java.util.List;

public interface DashboardService {

  DashboardStatsSummary getDashboardSummary(String userId);

  List<EnrollmentMapping> getEnrolledCourses(String userId);

  List<EnrollmentMapping>  getPendingCourses(String userId);

  List<EnrollmentMapping> getCompletedCourses(String userId);

  List<CourseSummary> getLikedCourses(String userId);
}
