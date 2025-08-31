package com.example.lms.service;

import com.example.lms.domain.Enrollment;
import com.example.lms.repo.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {
  private final EnrollmentRepository enrollmentRepository;
  public EnrollmentService(EnrollmentRepository enrollmentRepository) { this.enrollmentRepository = enrollmentRepository; }

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

  public Enrollment patch(String id, Integer progressPercent, String status) {
    Enrollment e = enrollmentRepository.findById(id).orElseThrow();
    if (progressPercent != null) e.setProgressPercent(progressPercent);
    if (status != null && !status.isBlank()) e.setStatus(status);
    e.setLastAccessedAt(OffsetDateTime.now());
    return enrollmentRepository.save(e);
  }
}
