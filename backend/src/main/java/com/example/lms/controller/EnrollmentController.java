package com.example.lms.controller;

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
  public EnrollmentMapping enroll(@RequestBody Map<String, String> body) {
    return enrollmentService.enroll(body.get("userId"), body.get("courseId"));
  }

  @PostMapping("unenroll")
  public EnrollmentMapping unenroll(@RequestBody Map<String, String> body) {
    return enrollmentService.enroll(body.get("userId"), body.get("courseId"));
  }

  @GetMapping("/{id}")
  public EnrollmentMapping getById(@PathVariable String id) {
    return enrollmentService.getById(id);
  }

  @GetMapping("/{id}/with-course")
  public Map<String, Object> getWithCourse(@PathVariable String id) {
    return enrollmentService.getWithCourse(id);
  }

  @PutMapping("/{id}/progress")
  public EnrollmentMapping updateProgress(@PathVariable String id, @RequestBody Map<String, Object> progressData) {
    return enrollmentService.updateProgress(id, progressData);
  }

  @PostMapping("/{id}/complete")
  public EnrollmentMapping completeCourse(@PathVariable String id) {
    return enrollmentService.completeCourse(id);
  }

}
