package com.example.lms.controller;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {
  private final CourseService courseService;
  public CourseController(CourseService courseService) { this.courseService = courseService; }

  @GetMapping("/enrolled")
  public List<CourseSummary> enrolled(@RequestParam String userId) {
    return courseService.getEnrolledCourses(userId);
  }

  @GetMapping("/continue")
  public Optional<CourseSummary> continueCourse(@RequestParam String userId) {
    return courseService.getContinueCourse(userId);
  }

  @GetMapping("/getCourseById/{courseId}")
  public CourseSummary getCourseById(@PathVariable(required = true) Long courseId,@RequestParam String userId) {
    return courseService.search(courseId);
  }
}
