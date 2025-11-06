package com.example.lms.service;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;

import java.util.List;
import java.util.Map;

public interface EnrollmentService {

  List<EnrollmentMapping> byUser(String userId);

  EnrollmentMapping enroll(String userId, Long courseId, String enrollmentType);

  EnrollmentMapping getById(Long enrollmentId);

  EnrollmentDetails updateProgress(Long enrollmentId, Long moduleId, Map<String, Object> progressData);

  List<EnrollmentMapping> bulkEnroll(List<String> emailIdList, List<Long> courseIdList, String enrollmentType, String userId);

  EnrollmentMapping completeCourse(Long enrollmentId);

  void unEnroll(String userId, Long courseId);
}
