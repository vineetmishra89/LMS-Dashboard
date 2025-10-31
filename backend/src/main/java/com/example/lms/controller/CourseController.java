package com.example.lms.controller;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.CourseCardDetailDto;
import com.example.lms.dto.TrainingOptionDto;
import com.example.lms.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @GetMapping("/courseCard/viewType/{viewType}")
  public List<CourseCardDetailDto> getCourseCardList(@PathVariable(required = true) String viewType, @RequestParam(required = false) String category) {
    return courseService.getCourseCardList(viewType,category);
  }

  @GetMapping("/list-for-assignment")
  public List<TrainingOptionDto> getTrainingsForAssignment() {
    List<CourseSummary> allCourses = courseService.getAll();
    return allCourses.stream()
      .map(course -> new TrainingOptionDto(
        course.getTrainingId(),
        course.getTopics() != null ? course.getTopics() : "Untitled Training",
        course.getCategory(),
        course.getLevel()
      ))
      .collect(Collectors.toList());
  }
}
