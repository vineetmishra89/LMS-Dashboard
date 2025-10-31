package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentDetailsId;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentDetailsRepository;
import com.example.lms.repo.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {
  private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
  
  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentDetailsRepository enrollmentDetailsRepository;
  private final CourseRepository courseRepository;
  private final EmployeeHierarchyService employeeHierarchyService;

  public EnrollmentService(EnrollmentRepository enrollmentRepository,EnrollmentDetailsRepository enrollmentDetailsRepository, CourseRepository courseRepository, EmployeeHierarchyService employeeHierarchyService) {
    this.enrollmentRepository = enrollmentRepository;
    this.enrollmentDetailsRepository = enrollmentDetailsRepository;
    this.courseRepository = courseRepository;
    this.employeeHierarchyService = employeeHierarchyService;
  }

  public List<EnrollmentMapping> byUser(String userId) {
    return enrollmentRepository.findByUserId(userId);
  }

  public EnrollmentMapping enroll(String userId, Long courseId, String enrollmentType) {
    CourseSummary courseSummary = courseRepository.findById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

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
    return enrollmentRepository.findById(enrollmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
  }

  public EnrollmentDetails updateProgress(Long enrollmentId, Long moduleId, Map<String, Object> progressData) {
    EnrollmentDetailsId enrollmentDetailsId = new EnrollmentDetailsId();
    enrollmentDetailsId.setTrainingEmrollmentId(enrollmentId);
    enrollmentDetailsId.setModuleId(moduleId);
    EnrollmentDetails enrollmentDetails = enrollmentDetailsRepository.findById(enrollmentDetailsId)
        .orElseThrow(() -> new ResourceNotFoundException("EnrollmentDetails", "enrollmentId-moduleId", enrollmentId + "-" + moduleId));

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
    if (emailIdList == null || emailIdList.isEmpty()) {
      throw new ValidationException("emailIdList", "Email list cannot be null or empty");
    }
    if (courseIdList == null || courseIdList.isEmpty()) {
      throw new ValidationException("courseIdList", "Course list cannot be null or empty");
    }

    if (employeeHierarchyService.isRo(userId)) {
      logger.info("Performing authorization check for RO: {}", userId);
      try {
        EmployeeHierarchyResponseDto hierarchy = employeeHierarchyService.getEmployeeHierarchy(userId);
        Set<String> authorizedEmails = hierarchy.employees().stream()
          .map(EmployeeDetailsDto::emailId)
          .collect(Collectors.toSet());
        
        List<String> unauthorizedEmails = emailIdList.stream()
          .filter(email -> !authorizedEmails.contains(email))
          .collect(Collectors.toList());
        
        if (!unauthorizedEmails.isEmpty()) {
          logger.error("Authorization failed: User {} attempted to assign to unauthorized users: {}", userId, unauthorizedEmails);
          throw new ValidationException("authorization", "You are not authorized to assign trainings to the following users: " + String.join(", ", unauthorizedEmails));
        }
        logger.info("Authorization check passed for RO: {}", userId);
      } catch (ValidationException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error during authorization check for user: {}", userId, e);
        throw new ValidationException("authorization", "Failed to verify authorization: " + e.getMessage());
      }
    }

    List<CourseSummary> courses = courseRepository.findAllById(courseIdList);

    if (courses.size() != courseIdList.size()) {
      throw new ResourceNotFoundException("Some courses not found. Expected: " + courseIdList.size() + ", Found: " + courses.size());
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
        enrollment.setEnrolledByEmailId(userId);

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
    EnrollmentMapping e = enrollmentRepository.findById(enrollmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
    e.setStatus("COMPLETED");

    return enrollmentRepository.save(e);
  }
}
