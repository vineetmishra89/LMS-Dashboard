package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EnrollmentService {
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;

  public EnrollmentService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
  }

  public List<EnrollmentMapping> byUser(String userId) {
    return enrollmentRepository.findByUserId(userId);
  }

  public EnrollmentMapping enroll(String userId, String courseId) {
    EnrollmentMapping e = new EnrollmentMapping();
    e.setId(UUID.randomUUID().toString());
    e.setUserId(userId);
    e.setCourseId(courseId);
    e.setProgressPercent(0);
    e.setStatus("active");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public EnrollmentMapping getById(String id) {
    return enrollmentRepository.findById(id).orElseThrow();
  }

  public Map<String, Object> getWithCourse(String id) {
    EnrollmentMapping enrollment = enrollmentRepository.findById(id).orElseThrow();
    CourseSummary course = courseRepository.findById(enrollment.getCourseId()).orElse(null);

    Map<String, Object> result = new HashMap<>();
    result.put("enrollment", enrollment);
    result.put("course", course);
    return result;
  }

  public EnrollmentMapping updateProgress(String id, Map<String, Object> progressData) {
    EnrollmentMapping e = enrollmentRepository.findById(id).orElseThrow();

    if (progressData.containsKey("overallProgress")) {
      Integer progress = ((Number) progressData.get("overallProgress")).intValue();
      e.setProgressPercent(progress);
    }

    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Map<String, Object> getStats(String userId) {
    List<EnrollmentMapping> enrollments = enrollmentRepository.findByUserId(userId);

    long totalEnrollments = enrollments.size();
    long activeEnrollments = enrollments.stream().filter(e -> "active".equals(e.getStatus())).count();
    long completedEnrollments = enrollments.stream().filter(e -> "completed".equals(e.getStatus())).count();
    double avgProgress = enrollments.stream()
        .filter(e -> e.getProgressPercent() != null)
        .mapToInt(EnrollmentMapping::getProgressPercent)
        .average()
        .orElse(0.0);

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalEnrollments", totalEnrollments);
    stats.put("activeEnrollments", activeEnrollments);
    stats.put("completedEnrollments", completedEnrollments);
    stats.put("averageProgress", Math.round(avgProgress));

    return stats;
  }

  public EnrollmentMapping completeCourse(String id) {
    EnrollmentMapping e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("completed");
    e.setProgressPercent(100);
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public EnrollmentMapping pauseEnrollment(String id, String reason) {
    EnrollmentMapping e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("paused");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public EnrollmentMapping resumeEnrollment(String id) {
    EnrollmentMapping e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("active");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public EnrollmentMapping patch(String id, Integer progressPercent, String status) {
    EnrollmentMapping e = enrollmentRepository.findById(id).orElseThrow();
    if (progressPercent != null) e.setProgressPercent(progressPercent);
    if (status != null && !status.isBlank()) e.setStatus(status);
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }
}
