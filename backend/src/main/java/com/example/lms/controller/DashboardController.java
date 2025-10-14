package com.example.lms.controller;

import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.DashboardStatsSummary;
import com.example.lms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

  @Autowired
  private DashboardService dashboardService;

  @GetMapping("/summary")
  public ResponseEntity<DashboardStatsSummary> getCourseSummary(@RequestParam String userId){
     DashboardStatsSummary summary = dashboardService.getDashboardSummary(userId);

     return ResponseEntity.ok(summary);
  }

  @GetMapping("/enrolledCourses")
  public ResponseEntity<List<CourseSummary>> getEnrolledCourses(@RequestParam String userId){
    List<CourseSummary> courseDetails = dashboardService.getEnrolledCourses(userId);

    return ResponseEntity.ok(courseDetails);
  }

  @GetMapping("/pendingCourses")
  public ResponseEntity<List<CourseSummary>> getPendingCourses(@RequestParam String userId){
    List<CourseSummary> courseDetails = dashboardService.getPendingCourses(userId);

    return ResponseEntity.ok(courseDetails);
  }

  @GetMapping("/completedCourses")
  public ResponseEntity<List<CourseSummary>> getCompletedCourses(@RequestParam String userId){
    List<CourseSummary> courseDetails = dashboardService.getCompletedCourses(userId);

    return ResponseEntity.ok(courseDetails);
  }

  @GetMapping("/likedCourses")
  public ResponseEntity<List<CourseSummary>> getLikedCourses(@RequestParam String userId){
    List<CourseSummary> courseDetails = dashboardService.getLikedCourses(userId);

    return ResponseEntity.ok(courseDetails);
  }
}
