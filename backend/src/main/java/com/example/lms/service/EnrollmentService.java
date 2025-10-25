package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentDetailsId;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentDetailsRepository;
import com.example.lms.repo.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentDetailsRepository enrollmentDetailsRepository;
  private final CourseRepository courseRepository;

  public EnrollmentService(EnrollmentRepository enrollmentRepository,EnrollmentDetailsRepository enrollmentDetailsRepository, CourseRepository courseRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.enrollmentDetailsRepository = enrollmentDetailsRepository;
    this.courseRepository = courseRepository;
  }

  public List<EnrollmentMapping> byUser(String userId) {
    return enrollmentRepository.findByUserId(userId);
  }

  public EnrollmentMapping enroll(String userId, Long courseId, String enrollmentType) {
    CourseSummary courseSummary = courseRepository.findById(courseId)
        .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

    EnrollmentMapping e = new EnrollmentMapping();
    e.setUserId(userId);
    e.setCourseSummary(courseSummary);
    e.setStatus("Enrolled");
    e.setEnrolledTs(OffsetDateTime.now());
    e.setEnrollmentType(enrollmentType.toUpperCase());
    e.setProgressPercent(0L);
    e.setCreatedTs(OffsetDateTime.now());
    e.setUpdatedTs(OffsetDateTime.now());
    e.setCreatedBy(userId);
    e.setUpdatedBy(userId);

    List<EnrollmentDetails> enrollmentDetailsList = new ArrayList<>();
    if (courseSummary.getLmsTrainingDetails() != null) {
      for (var courseDetail : courseSummary.getLmsTrainingDetails()) {
        EnrollmentDetailsId detailsId = new EnrollmentDetailsId();
        detailsId.setModuleId(courseDetail.getModuleId());

        EnrollmentDetails details = new EnrollmentDetails();
        details.setEnrollmentDetailsId(detailsId);
        details.setStatus("Enrolled");
        details.setEnrollmentMapping(e);
        details.setCourseDetail(courseDetail);
        details.setCreatedTs(OffsetDateTime.now());
        details.setUpdatedTs(OffsetDateTime.now());
        details.setCreatedBy(userId);
        details.setUpdatedBy(userId);
        details.setCurrentLearningTs(0);

        enrollmentDetailsList.add(details);
      }
    }

    e.setEnrollmentDetailsList(enrollmentDetailsList);

    return enrollmentRepository.save(e);
  }

  public EnrollmentMapping getById(Long enrollmentId) {
    return enrollmentRepository.findById(enrollmentId).orElseThrow();
  }

  public EnrollmentDetails updateProgress(Long enrollmentId, Long moduleId, Map<String, Object> progressData) {
    EnrollmentDetailsId enrollmentDetailsId = new EnrollmentDetailsId();
    enrollmentDetailsId.setTrainingEmrollmentId(enrollmentId);
    enrollmentDetailsId.setModuleId(moduleId);
    EnrollmentDetails enrollmentDetails = enrollmentDetailsRepository.findById(enrollmentDetailsId).orElseThrow();

    if (progressData.containsKey("overallProgress")) {
      Integer progress = ((Number) progressData.get("overallProgress")).intValue();
      enrollmentDetails.setCurrentLearningTs(progress);
    }

    enrollmentDetails.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentDetailsRepository.save(enrollmentDetails);
  }

  public Map<String, Object> getStats(String userId) {
    List<EnrollmentMapping> enrollments = enrollmentRepository.findByUserId(userId);

    long totalEnrollments = enrollments.size();
    long activeEnrollments = enrollments.stream().filter(e -> "active".equals(e.getStatus())).count();
    long completedEnrollments = enrollments.stream().filter(e -> "completed".equals(e.getStatus())).count();

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalEnrollments", totalEnrollments);
    stats.put("activeEnrollments", activeEnrollments);
    stats.put("completedEnrollments", completedEnrollments);

    return stats;
  }

  @Transactional
  public List<EnrollmentMapping> bulkEnroll(List<String> emailIdList, List<Long> courseIdList, String enrollmentType, String userId) {
    if (emailIdList == null || emailIdList.isEmpty() || courseIdList == null || courseIdList.isEmpty()) {
      throw new IllegalArgumentException("Email list and course list cannot be null or empty");
    }

    List<CourseSummary> courses = courseRepository.findAllById(courseIdList);
    
    if (courses.size() != courseIdList.size()) {
      throw new RuntimeException("Some courses not found. Expected: " + courseIdList.size() + ", Found: " + courses.size());
    }
    
    Map<Long, CourseSummary> courseMap = courses.stream()
        .collect(Collectors.toMap(CourseSummary::getTrainingId, course -> course));

    List<EnrollmentMapping> allEnrollments = new ArrayList<>();
    OffsetDateTime now = OffsetDateTime.now();
    String enrollmentTypeUpper = enrollmentType.toUpperCase();

    for (String emailId : emailIdList) {
      for (Long courseId : courseIdList) {
        CourseSummary courseSummary = courseMap.get(courseId);
        
        EnrollmentMapping enrollment = new EnrollmentMapping();
        enrollment.setUserId(emailId);
        enrollment.setCourseSummary(courseSummary);
        enrollment.setStatus("Enrolled");
        enrollment.setEnrolledTs(now);
        enrollment.setEnrollmentType(enrollmentTypeUpper);
        enrollment.setProgressPercent(0L);
        enrollment.setCreatedTs(now);
        enrollment.setUpdatedTs(now);
        enrollment.setCreatedBy(userId);
        enrollment.setUpdatedBy(userId);

        List<EnrollmentDetails> detailsList = new ArrayList<>();
        if (courseSummary.getLmsTrainingDetails() != null) {
          for (var courseDetail : courseSummary.getLmsTrainingDetails()) {
            EnrollmentDetailsId detailsId = new EnrollmentDetailsId();
            detailsId.setModuleId(courseDetail.getModuleId());

            EnrollmentDetails details = new EnrollmentDetails();
            details.setEnrollmentDetailsId(detailsId);
            details.setStatus("Enrolled");
            details.setEnrollmentMapping(enrollment);
            details.setCourseDetail(courseDetail);
            details.setCreatedTs(now);
            details.setUpdatedTs(now);
            details.setCreatedBy(userId);
            details.setUpdatedBy(userId);
            details.setCurrentLearningTs(0);

            detailsList.add(details);
          }
        }
        
        enrollment.setEnrollmentDetailsList(detailsList);
        allEnrollments.add(enrollment);
      }
    }

    return enrollmentRepository.saveAll(allEnrollments);
  }

  public EnrollmentMapping completeCourse(Long enrollmentId) {
    EnrollmentMapping e = enrollmentRepository.findById(enrollmentId).orElseThrow();
    e.setStatus("COMPLETED");

    return enrollmentRepository.save(e);
  }
}
