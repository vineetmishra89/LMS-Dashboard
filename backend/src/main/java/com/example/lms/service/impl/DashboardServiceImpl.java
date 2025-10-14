package com.example.lms.service.impl;

import com.example.lms.constant.EnrollmentStatus;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

  @Autowired
  private EnrollmentRepository courseRepository;

  @Override
  public DashboardStatsSummary getDashboardSummary(String userId) {
    long completedCourse = courseRepository.countByUserIdAndStatus(userId, EnrollmentStatus.COMPLETED.getStatus());
    long enrolledCourses = courseRepository.countByUserIdAndStatus(userId, EnrollmentStatus.ENROLLED.getStatus());
    long pendingCourses = courseRepository.countByUserIdAndStatus(userId, EnrollmentStatus.PENDING.getStatus());

    return DashboardStatsSummary.builder().completedCourse(completedCourse).pendingCourse(pendingCourses).enrolledCourse(enrolledCourses).build();
  }

  @Override
  public List<EnrollmentMapping> getEnrolledCourses(String userId) {
    return courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ENROLLED.getStatus());
  }

  @Override
  public List<EnrollmentMapping> getPendingCourses(String userId) {
    return courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.PENDING.getStatus());
  }

  @Override
  public List<EnrollmentMapping> getCompletedCourses(String userId) {
    return courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.COMPLETED.getStatus());
  }

  @Override
  public List<CourseSummary> getLikedCourses(String userId) {
    return List.of();
  }
}
