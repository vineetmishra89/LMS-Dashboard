package com.example.lms.controller;

import com.example.lms.domain.Course;
import com.example.lms.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {
  private final CourseService courseService;
  public CourseController(CourseService courseService) { this.courseService = courseService; }

  @GetMapping
  public List<Course> list(@RequestParam(required = false) String category,
                           @RequestParam(required = false) String topic,
                           @RequestParam(required = false) String instructor) {
    return courseService.search(category, topic, instructor);
  }

  @GetMapping("/enrolled")
  public List<Course> enrolled(@RequestParam UUID userId) {
    return courseService.getEnrolledCourses(userId);
  }

  @GetMapping("/continue")
  public Optional<Course> continueCourse(@RequestParam UUID userId) {
    return courseService.getContinueCourse(userId);
  }
}
