package com.example.lms.service;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.repo.CourseDetailRepository;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
  private static final Logger log = LoggerFactory.getLogger(CourseService.class);
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseDetailRepository courseDetailRepository;

  public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,CourseDetailRepository courseDetailRepository) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseDetailRepository = courseDetailRepository;
  }

  public List<CourseSummary> search(String category, String topic, String instructor) {
    Specification<CourseSummary> spec = Specification.where(null);
    List<CourseSummary> courses = new ArrayList<>();
    if (category != null && !category.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
    }
    if (instructor != null && !instructor.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("instructorName")), instructor.toLowerCase()));
    }
    if (topic != null && !topic.isBlank()) {
      spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("topics")), topic.toLowerCase()));
    }
    if (spec == null) return courseRepository.findAll();

    try{
      courses = courseRepository.findAll(spec);
    }catch(Exception ex){
      log.error("Exception occurred : ",ex);
    }
    return courses;
  }

  public List<CourseSummary> getAll() {
    return courseRepository.findAll();
  }

  public List<CourseSummary> getEnrolledCourses(String userId) {
    List<EnrollmentMapping> enrollments = enrollmentRepository.findByUserId(userId);
    Set<String> courseIds = enrollments.stream().map(EnrollmentMapping::getCourseId).collect(Collectors.toSet());
    if (courseIds.isEmpty()) return List.of();
    return courseRepository.findAllById(courseIds);
  }

  public Optional<CourseSummary> getContinueCourse(String userId) {
    return enrollmentRepository.findByUserId(userId).stream()
      .filter(e -> "active".equalsIgnoreCase(e.getStatus()) && e.getProgressPercent() != null && e.getProgressPercent() < 100)
      .sorted(Comparator.comparing(EnrollmentMapping::getLastAccessedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
      .map(EnrollmentMapping::getCourseId)
      .findFirst()
      .flatMap(courseRepository::findById);
  }

  public CourseDetail search(String courseId) {
    CourseDetail course = null;

    try{
      course = courseDetailRepository.findById(courseId).orElse(null);
    }catch(Exception ex){
      log.error("Exception occurred : ",ex);
    }
    return course;
  }
}
