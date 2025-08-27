package com.example.lms.service;

import com.example.lms.dto.AnalyticsSummaryDto;
import com.example.lms.repo.CertificateRepository;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.repo.LearningHoursRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnalyticsService {
  private final EnrollmentRepository enrollmentRepository;
  private final CertificateRepository certificateRepository;
  private final LearningHoursRepository learningHoursRepository;

  public AnalyticsService(EnrollmentRepository enrollmentRepository, CertificateRepository certificateRepository, LearningHoursRepository learningHoursRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.certificateRepository = certificateRepository;
    this.learningHoursRepository = learningHoursRepository;
  }

  public AnalyticsSummaryDto getSummary(UUID userId) {
    long enrolled = enrollmentRepository.findByUserId(userId).size();
    long completed = (int) enrollmentRepository.findByUserId(userId).stream().filter(e -> "completed".equalsIgnoreCase(e.getStatus())).count();
    double hours = learningHoursRepository.findByUserId(userId.toString()).stream().mapToDouble(h -> h.getTotalHours()).sum();
    return new AnalyticsSummaryDto(completed, enrolled, hours);
  }
}
