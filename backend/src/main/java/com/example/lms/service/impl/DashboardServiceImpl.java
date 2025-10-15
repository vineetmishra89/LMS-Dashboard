package com.example.lms.service.impl;

import com.example.lms.constant.EnrollmentStatus;
import com.example.lms.domain.*;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
  public List<DashboardSummary> getEnrolledCourses(String userId) {
    List<EnrollmentMapping> enrollmentMappingList = courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ENROLLED.getStatus());
    List<DashboardSummary> summaryList = new ArrayList<>();
    enrollmentMappingList.forEach(enrollmentMapping -> {
      DashboardSummary summary = new DashboardSummary();
      summary.setCatgory(enrollmentMapping.getCourseSummary().getCategory());
      summary.setDescription(enrollmentMapping.getCourseSummary().getLevel());
      summary.setDetails(enrollmentMapping.getCourseSummary().getDetails());
      summary.setTrainingName(enrollmentMapping.getCourseSummary().getTopics());
      summary.setDuration(enrollmentMapping.getCourseSummary().getDuration());
      summary.setProgress(0);
    });
    return summaryList;
  }

  @Override
  public List<DashboardSummary> getPendingCourses(String userId) {
    List<EnrollmentMapping> enrollmentMappingList = courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.PENDING.getStatus());
    List<DashboardSummary> summaryList = new ArrayList<>();
    enrollmentMappingList.forEach(enrollmentMapping -> {
      DashboardSummary summary = new DashboardSummary();
      summary.setCatgory(enrollmentMapping.getCourseSummary().getCategory());
      summary.setDescription(enrollmentMapping.getCourseSummary().getLevel());
      summary.setDetails(enrollmentMapping.getCourseSummary().getDetails());
      summary.setTrainingName(enrollmentMapping.getCourseSummary().getTopics());
      summary.setDuration(enrollmentMapping.getCourseSummary().getDuration());
      summary.setProgress(enrollmentMapping.getCourseSummary().getCourseProgressPercentage());
    });
    return summaryList;
  }

  @Override
  public List<DashboardSummary> getCompletedCourses(String userId) {
    List<EnrollmentMapping> enrollmentMappingList = courseRepository.findByUserIdAndStatus(userId, EnrollmentStatus.COMPLETED.getStatus());
    List<DashboardSummary> summaryList = new ArrayList<>();
    enrollmentMappingList.forEach(enrollmentMapping -> {
      DashboardSummary summary = new DashboardSummary();
      summary.setCatgory(enrollmentMapping.getCourseSummary().getCategory());
      summary.setDescription(enrollmentMapping.getCourseSummary().getLevel());
      summary.setDetails(enrollmentMapping.getCourseSummary().getDetails());
      summary.setTrainingName(enrollmentMapping.getCourseSummary().getTopics());
      summary.setDuration(enrollmentMapping.getCourseSummary().getDuration());
      summary.setProgress(100);
    });
    return summaryList;
  }

  @Override
  public List<CourseSummary> getLikedCourses(String userId) {
    return List.of();
  }
}
