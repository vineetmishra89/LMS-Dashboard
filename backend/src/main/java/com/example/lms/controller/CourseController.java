package com.example.lms.controller;

import com.example.lms.domain.TrainingDetails;
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

  @GetMapping
  public List<TrainingDetails> list(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String topic,
                                    @RequestParam(required = false) String instructor) {
    return courseService.search(category, topic, instructor);
  }

  @GetMapping("/enrolled")
  public List<TrainingDetails> enrolled(@RequestParam String userId) {
    return courseService.getEnrolledCourses(userId);
  }

  @GetMapping("/continue")
  public Optional<TrainingDetails> continueCourse(@RequestParam String userId) {
    return courseService.getContinueCourse(userId);
  }

  @GetMapping("/getCourseById/{courseId}")
  public TrainingDetails getCourseById(@PathVariable(required = true) String courseId) {
    return courseService.search(courseId);
  }
}
