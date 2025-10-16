package com.example.lms.service;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.dto.CourseCardDetailDto;
import com.example.lms.repo.CourseCardRepository;
import com.example.lms.repo.CourseDetailRepository;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EnrollmentRepository;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
  private static final Logger log = LoggerFactory.getLogger(CourseService.class);
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseCardRepository courseCardRepository;
  @Getter
  @Value("${course.interval}")
  private String courseInterval = null;

  public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,CourseCardRepository courseCardRepository) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseCardRepository = courseCardRepository;
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
    Set<Long> courseIds = enrollments.stream().map(enrollmentMapping -> enrollmentMapping.getCourseSummary().getTrainingId()).collect(Collectors.toSet());
    if (courseIds.isEmpty()) return List.of();
    return courseRepository.findAllById(courseIds);
  }

  public Optional<CourseSummary> getContinueCourse(String userId) {
    return enrollmentRepository.findByUserId(userId).stream()
      .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()) && e.getProgressPercent() != null && e.getProgressPercent() < 100)
      .sorted(Comparator.comparing(EnrollmentMapping::getLastAccessedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
      .map(enrollmentMapping -> enrollmentMapping.getCourseSummary().getTrainingId())
      .findFirst()
      .flatMap(courseRepository::findById);
  }

  public CourseSummary search(Long courseId) {
    CourseSummary course = null;

    try{
      course = courseRepository.findById(courseId).orElse(null);
    }catch(Exception ex){
      log.error("Exception occurred : ",ex);
    }
    return course;
  }

  public List<CourseCardDetailDto> getCourseCardList(String viewType, String category) {
    List<Object[]> courseCardDetailList = null;
    try{
      if(StringUtils.isNotBlank(viewType)){
        switch(viewType) {
          case "View":
            courseCardDetailList = courseCardRepository.findCourceCardDetailsByView(getCourseInterval());
            break;
          case "Enrolled":
            courseCardDetailList = courseCardRepository.findCourseCardDetailsByEnrollment(getCourseInterval());
            break;
          case "Rate":
            courseCardDetailList = courseCardRepository.findCourceCardDetailsByTopRate(getCourseInterval());
            break;
          case "Category":
            courseCardDetailList = courseCardRepository.findCourceCardDetailsByCourseCategory(category,getCourseInterval());
            break;
          default:
            break;
        }
        return courseCardDetailList != null ? courseCardDetailList.stream()
          .map(row -> new CourseCardDetailDto((Long) row[0],(String) row[1], (String) row[2], (Long) row[3], (String) row[4], (Long) row[5]))
          .collect(Collectors.toList()) : null;
      }
    }catch(Exception ex){
      log.error("Exception occurred : ",ex);
    }
    return null;
  }
}
