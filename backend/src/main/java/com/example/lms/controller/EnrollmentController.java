package com.example.lms.controller;

import com.example.lms.domain.Enrollment;
import com.example.lms.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
public class EnrollmentController {
  private final EnrollmentService enrollmentService;
  public EnrollmentController(EnrollmentService enrollmentService) { this.enrollmentService = enrollmentService; }

  @GetMapping
  public List<Enrollment> byUser(@RequestParam String userId) {
    return enrollmentService.byUser(userId);
  }

  @PostMapping
  public Enrollment enroll(@RequestBody Map<String, String> body) {
    return enrollmentService.enroll(body.get("userId"), body.get("courseId"));
  }

  @GetMapping("/{id}")
  public Enrollment getById(@PathVariable String id) {
    return enrollmentService.getById(id);
  }

  @GetMapping("/{id}/with-course")
  public Map<String, Object> getWithCourse(@PathVariable String id) {
    return enrollmentService.getWithCourse(id);
  }

  @PutMapping("/{id}/progress")
  public Enrollment updateProgress(@PathVariable String id, @RequestBody Map<String, Object> progressData) {
    return enrollmentService.updateProgress(id, progressData);
  }

  @GetMapping("/stats")
  public Map<String, Object> getStats(@RequestParam String userId) {
    return enrollmentService.getStats(userId);
  }

  @PostMapping("/{id}/complete")
  public Enrollment completeCourse(@PathVariable String id) {
    return enrollmentService.completeCourse(id);
  }

  @PutMapping("/{id}/pause")
  public Enrollment pauseEnrollment(@PathVariable String id, @RequestBody Map<String, String> body) {
    String reason = body.get("reason");
    return enrollmentService.pauseEnrollment(id, reason);
  }

  @PutMapping("/{id}/resume")
  public Enrollment resumeEnrollment(@PathVariable String id) {
    return enrollmentService.resumeEnrollment(id);
  }

  @PatchMapping("/{id}")
  public Enrollment patch(@PathVariable String id, @RequestBody Map<String, Object> body) {
    Integer progress = body.get("progressPercent") == null ? null : ((Number) body.get("progressPercent")).intValue();
    String status = (String) body.get("status");
    return enrollmentService.patch(id, progress, status);
  }
}
