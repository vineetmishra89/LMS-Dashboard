package com.example.lms.controller;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.dto.BulkEnrollRequest;
import com.example.lms.dto.EnrollRequest;
import com.example.lms.dto.UpdateProgressRequest;
import com.example.lms.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
public class EnrollmentController {
  private final EnrollmentService enrollmentService;
  public EnrollmentController(EnrollmentService enrollmentService) { this.enrollmentService = enrollmentService; }

  @PostMapping("enroll")
  public EnrollmentMapping enroll(@Valid @RequestBody EnrollRequest request) {
    String enrollmentType = (request.getEnrollmentType() == null) ? "Voluntary" : request.getEnrollmentType();
    return enrollmentService.enroll(request.getUserId(), request.getCourseId(), enrollmentType);
  }

  @PostMapping("unenroll")
  public EnrollmentMapping unenroll(@RequestBody Map<String, Object> body) {
    return enrollmentService.enroll((String)body.get("userId"), (Long)body.get("courseId"),null);
  }

  @GetMapping("/{enrollmentId}")
  public EnrollmentMapping getById(@PathVariable Long enrollmentId) {
    return enrollmentService.getById(enrollmentId);
  }

  @PutMapping("/{enrollmentId}/module/{moduleId}/progress")
  public EnrollmentDetails updateProgress(@PathVariable Long enrollmentId, @PathVariable Long moduleId, @Valid @RequestBody UpdateProgressRequest request) {
    Map<String, Object> progressData = Map.of("overallProgress", request.getOverallProgress());
    return enrollmentService.updateProgress(enrollmentId, moduleId, progressData);
  }

  @PostMapping("/{enrollmentId}/complete")
  public EnrollmentMapping completeCourse(@PathVariable Long enrollmentId) {
    return enrollmentService.completeCourse(enrollmentId);
  }

  @PostMapping("/bulk-enroll")
  public List<EnrollmentMapping> bulkEnroll(@Valid @RequestBody BulkEnrollRequest request) {
    String enrollmentType = request.getEnrollmentType() != null ? request.getEnrollmentType() : "VOLUNTARY";
    return enrollmentService.bulkEnroll(request.getEmailIdList(), request.getCourseIdList(), enrollmentType, request.getUserId());
  }

}
