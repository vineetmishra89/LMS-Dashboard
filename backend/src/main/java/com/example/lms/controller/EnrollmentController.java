package com.example.lms.controller;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
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
    Integer courseIdInt = (Integer)body.get("courseId");
    Long courseId = Long.valueOf(courseIdInt);
    return enrollmentService.enroll((String)body.get("userId"), courseId,enrollmentType);
  }

  @PostMapping("unenroll")
  public ResponseEntity<Map<String,String>> unenroll(@RequestBody Map<String, Object> body) {
    enrollmentService.unEnroll((String)body.get("userId"), Long.valueOf((Integer)body.get("courseId")));
    return ResponseEntity.ok(Map.of("Status","SUCCESS"));
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
