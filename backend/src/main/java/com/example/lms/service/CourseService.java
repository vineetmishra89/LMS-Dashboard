package com.example.lms.service;

import com.example.lms.domain.Course;
import com.example.lms.domain.Enrollment;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;

  public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  public List<Course> search(String category, String topic, String instructor) {
    Specification<Course> spec = Specification.where(null);
    if (category != null && !category.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
    }
    if (instructor != null && !instructor.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("instructorName")), instructor.toLowerCase()));
    }
    if (topic != null && !topic.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.isMember(topic, root.get("topics")));
    }
    if (spec == null) return courseRepository.findAll();
    return courseRepository.findAll(spec);
  }

  public List<Course> getAll() {
    return courseRepository.findAll();
  }

  public List<Course> getEnrolledCourses(UUID userId) {
    List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
    Set<UUID> courseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toSet());
    if (courseIds.isEmpty()) return List.of();
    return courseRepository.findAllById(courseIds);
  }

  public Optional<Course> getContinueCourse(UUID userId) {
    return enrollmentRepository.findByUserId(userId).stream()
      .filter(e -> "active".equalsIgnoreCase(e.getStatus()) && e.getProgressPercent() != null && e.getProgressPercent() < 100)
      .sorted(Comparator.comparing(Enrollment::getLastAccessedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
      .map(Enrollment::getCourseId)
      .findFirst()
      .flatMap(courseRepository::findById);
  }
}
