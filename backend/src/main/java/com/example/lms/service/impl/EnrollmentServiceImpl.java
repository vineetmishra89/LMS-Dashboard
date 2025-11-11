package com.example.lms.service.impl;

import com.example.lms.constant.EnrollmentStatus;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentDetailsRepository;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.service.EnrollmentService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {
  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentDetailsRepository enrollmentDetailsRepository;
  private final CourseRepository courseRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,EnrollmentDetailsRepository enrollmentDetailsRepository, CourseRepository courseRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.enrollmentDetailsRepository = enrollmentDetailsRepository;
    this.courseRepository = courseRepository;
  }

  @PostConstruct
  public void checkProxy(){
    System.out.println(">>> EnrollmentServiceImpl class: "+ this.getClass());
  }

  @Override
  public List<EnrollmentMapping> byUser(String userId) {
    return enrollmentRepository.findByUserId(userId);
  }

  @Override
  public EnrollmentMapping enroll(String userId, Long courseId, String enrollmentType) {
    CourseSummary courseSummary = courseRepository.findById(courseId)
      .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

    EnrollmentMapping e = new EnrollmentMapping();
    e.setUserId(userId);
    e.setCourseSummary(courseSummary);
    e.setStatus(EnrollmentStatus.ENROLLED.getStatus());
    e.setEnrolledTs(OffsetDateTime.now());
    e.setEnrollmentType(enrollmentType.toUpperCase());
    e.setProgressPercent(0L);
    e.setCreatedTs(OffsetDateTime.now());
    e.setUpdatedTs(OffsetDateTime.now());
    e.setCreatedBy(userId);
    e.setUpdatedBy(userId);
    e.setEnrollmentDetailsList(new ArrayList<>());

    if (courseSummary.getLmsTrainingDetails() != null) {
      for (var courseDetail : courseSummary.getLmsTrainingDetails()) {
        EnrollmentDetails details = new EnrollmentDetails();
        details.setModuleId(courseDetail.getModuleId());
        details.setStatus(EnrollmentStatus.ENROLLED.getStatus());
        details.setEnrollmentMapping(e);
        details.setCourseDetail(courseDetail);
        details.setCreatedTs(OffsetDateTime.now());
        details.setUpdatedTs(OffsetDateTime.now());
        details.setCreatedBy(userId);
        details.setUpdatedBy(userId);
        e.addEnrollmentDetail(details);
      }
    }

    return enrollmentRepository.save(e);
  }

  @Override
  public EnrollmentMapping getById(Long enrollmentId) {
    return enrollmentRepository.findById(enrollmentId)
      .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
  }

  @Override
  public EnrollmentDetails updateProgress(Long enrollmentDetailsId, Map<String, Object> progressData) {
    EnrollmentDetails enrollmentDetails = enrollmentDetailsRepository.findById(enrollmentDetailsId)
      .orElseThrow(() -> new ResourceNotFoundException("enrollmentDetailsId", "enrollmentDetailsId", enrollmentDetailsId));

    if (progressData.containsKey("overallProgress")) {
      Integer progress = ((Number) progressData.get("overallProgress")).intValue();
      //enrollmentDetails.setCurrentLearningTs(progress);
    }

    //enrollmentDetails.setLastAccessedAt(OffsetDateTime.now());
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
  @Override
  public List<EnrollmentMapping> bulkEnroll(List<String> emailIdList, List<Long> courseIdList, String enrollmentType, String userId) {
    if (emailIdList == null || emailIdList.isEmpty()) {
      throw new ValidationException("emailIdList", "Email list cannot be null or empty");
    }
    if (courseIdList == null || courseIdList.isEmpty()) {
      throw new ValidationException("courseIdList", "Course list cannot be null or empty");
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

            EnrollmentDetails details = new EnrollmentDetails();
            details.setModuleId(courseDetail.getModuleId());
            details.setStatus("Enrolled");
            details.setEnrollmentMapping(enrollment);
            details.setCourseDetail(courseDetail);
            details.setCreatedTs(now);
            details.setUpdatedTs(now);
            details.setCreatedBy(userId);
            details.setUpdatedBy(userId);
           // details.setCurrentLearningTs(0);

            detailsList.add(details);
          }
        }

        enrollment.setEnrollmentDetailsList(detailsList);
        allEnrollments.add(enrollment);
      }
    }

    return enrollmentRepository.saveAll(allEnrollments);
  }

  @Override
  public EnrollmentMapping completeCourse(Long enrollmentId) {
    EnrollmentMapping e = enrollmentRepository.findById(enrollmentId)
      .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
    e.setStatus("COMPLETED");

    return enrollmentRepository.save(e);
  }

  @Override
  @Transactional
  public void unEnroll(Long enrollmentId) {
    enrollmentRepository.deleteEnrollDetailByEnrollmentId(enrollmentId);
    enrollmentRepository.deleteEnrollMappingByEnrollmentId(enrollmentId);
  }
}
