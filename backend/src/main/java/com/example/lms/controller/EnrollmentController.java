package com.example.lms.controller;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.dto.EnrollmentRequest;
import com.example.lms.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
public class EnrollmentController {
  private final EnrollmentService enrollmentService;

  @Autowired
  public EnrollmentController(EnrollmentService enrollmentService) { this.enrollmentService = enrollmentService; }

  @PostMapping("enroll")
  public EnrollmentMapping enroll(@RequestBody EnrollmentRequest enrollmentRequest) {
    String enrollmentType = (null == enrollmentRequest.getEnrollmentType()) ? "Voluntary" : enrollmentRequest.getEnrollmentType();
    return enrollmentService.enroll(enrollmentRequest.getUserId(), enrollmentRequest.getCourseId(),enrollmentType);
  }

  @PostMapping("unenroll/{enrollmentId}")
  public ResponseEntity<Map<String,String>> unenroll(@PathVariable Long enrollmentId) {
    enrollmentService.unEnroll(enrollmentId);
    return ResponseEntity.ok(Map.of("Status","SUCCESS"));
  }

  @GetMapping("/{enrollmentId}")
  public EnrollmentMapping getById(@PathVariable Long enrollmentId) {
    EnrollmentMapping mapping = enrollmentService.getById(enrollmentId);;
    return mapping;
  }

  @PutMapping("/{enrollmentDetailsId}/module/{moduleId}/progress")
  public EnrollmentDetails updateProgress(@PathVariable Long enrollmentDetailsId, @PathVariable Long moduleId, @RequestBody Map<String, Object> progressData) {
    return enrollmentService.updateProgress(enrollmentDetailsId, progressData);
  }

  @PostMapping("/{enrollmentId}/complete")
  public EnrollmentMapping completeCourse(@PathVariable Long enrollmentId) {
    return enrollmentService.completeCourse(enrollmentId);
  }

  @PostMapping("/bulk-enroll")
  public List<EnrollmentMapping> bulkEnroll(@RequestBody Map<String, Object> body) {
    String userId = (String) body.get("userId");
    @SuppressWarnings("unchecked")
    List<String> emailIdList = (List<String>) body.get("emailIdList");
    @SuppressWarnings("unchecked")
    List<Long> courseIdList = ((List<Number>) body.get("courseIdList")).stream()
        .map(Number::longValue)
        .collect(java.util.stream.Collectors.toList());
    String enrollmentType = body.get("enrollmentType") != null ?
        (String) body.get("enrollmentType") : "VOLUNTARY";

    return enrollmentService.bulkEnroll(emailIdList, courseIdList, enrollmentType, userId);
  }
}
