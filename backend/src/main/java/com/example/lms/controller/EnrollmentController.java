package com.example.lms.controller;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.service.EnrollmentService;
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
  public EnrollmentMapping enroll(@RequestBody Map<String, Object> body) {
    String enrollmentType = (null == body.get("enrollmentType") ? "Voluntary" : (String)body.get("enrollmentType"));
    return enrollmentService.enroll((String)body.get("userId"), (Long)body.get("courseId"),enrollmentType);
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
  public EnrollmentDetails updateProgress(@PathVariable Long enrollmentId, @PathVariable Long moduleId, @RequestBody Map<String, Object> progressData) {
    return enrollmentService.updateProgress(enrollmentId, moduleId, progressData);
  }

  @PostMapping("/{enrollmentId}/complete")
  public EnrollmentMapping completeCourse(@PathVariable Long enrollmentId) {
    return enrollmentService.completeCourse(enrollmentId);
  }

}
