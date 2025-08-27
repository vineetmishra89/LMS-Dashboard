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
  public List<Enrollment> byUser(@RequestParam UUID userId) {
    return enrollmentService.byUser(userId);
  }

  @PostMapping
  public Enrollment enroll(@RequestBody Map<String, String> body) {
    UUID userId = UUID.fromString(body.get("userId"));
    UUID courseId = UUID.fromString(body.get("courseId"));
    return enrollmentService.enroll(userId, courseId);
  }

  @PatchMapping("/{id}")
  public Enrollment patch(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
    Integer progress = body.get("progressPercent") == null ? null : ((Number) body.get("progressPercent")).intValue();
    String status = (String) body.get("status");
    return enrollmentService.patch(id, progress, status);
  }
}
