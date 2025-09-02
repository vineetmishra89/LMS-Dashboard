package com.example.lms.service;

import com.example.lms.domain.Course;
import com.example.lms.domain.Enrollment;
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

  public List<Enrollment> byUser(String userId) {
    return enrollmentRepository.findByUserId(userId);
  }

  public Enrollment enroll(String userId, String courseId) {
    Enrollment e = new Enrollment();
    e.setId(UUID.randomUUID().toString());
    e.setUserId(userId);
    e.setCourseId(courseId);
    e.setProgressPercent(0);
    e.setStatus("active");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Enrollment getById(String id) {
    return enrollmentRepository.findById(id).orElseThrow();
  }

  public Map<String, Object> getWithCourse(String id) {
    Enrollment enrollment = enrollmentRepository.findById(id).orElseThrow();
    Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
    
    Map<String, Object> result = new HashMap<>();
    result.put("enrollment", enrollment);
    result.put("course", course);
    return result;
  }

  public Enrollment updateProgress(String id, Map<String, Object> progressData) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    
    if (progressData.containsKey("overallProgress")) {
      Integer progress = ((Number) progressData.get("overallProgress")).intValue();
      e.setProgressPercent(progress);
    }
    
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Map<String, Object> getStats(String userId) {
    List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
    
    long totalEnrollments = enrollments.size();
    long activeEnrollments = enrollments.stream().filter(e -> "active".equals(e.getStatus())).count();
    long completedEnrollments = enrollments.stream().filter(e -> "completed".equals(e.getStatus())).count();
    double avgProgress = enrollments.stream()
        .filter(e -> e.getProgressPercent() != null)
        .mapToInt(Enrollment::getProgressPercent)
        .average()
        .orElse(0.0);
    
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalEnrollments", totalEnrollments);
    stats.put("activeEnrollments", activeEnrollments);
    stats.put("completedEnrollments", completedEnrollments);
    stats.put("averageProgress", Math.round(avgProgress));
    
    return stats;
  }

  public Enrollment completeCourse(String id) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("completed");
    e.setProgressPercent(100);
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Enrollment pauseEnrollment(String id, String reason) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("paused");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Enrollment resumeEnrollment(String id) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    e.setStatus("active");
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }

  public Enrollment patch(String id, Integer progressPercent, String status) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    if (progressPercent != null) e.setProgressPercent(progressPercent);
    if (status != null && !status.isBlank()) e.setStatus(status);
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }
}
