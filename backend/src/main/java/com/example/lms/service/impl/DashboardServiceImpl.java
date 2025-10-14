package com.example.lms.service.impl;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;
import com.example.lms.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
  @Override
  public DashboardStatsSummary getDashboardSummary(String userId) {
    return null;
  }

  @Override
  public List<CourseSummary> getEnrolledCourses(String userId) {
    return List.of();
  }

  @Override
  public List<CourseSummary> getPendingCourses(String userId) {
    return List.of();
  }

  @Override
  public List<CourseSummary> getCompletedCourses(String userId) {
    return List.of();
  }

  @Override
  public List<CourseSummary> getLikedCourses(String userId) {
    return List.of();
  }
}
