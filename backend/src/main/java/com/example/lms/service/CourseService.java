package com.example.lms.service;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.dto.CourseCardDetailDto;
import com.example.lms.exception.DatabaseException;
import com.example.lms.exception.ResourceNotFoundException;
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

import java.math.BigDecimal;
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
      return courseRepository.findAll(spec);
    }catch(Exception ex){
      log.error("Database error while searching courses", ex);
      throw new DatabaseException("Failed to search courses", ex);
    }
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
      .sorted(Comparator.comparing(EnrollmentMapping::getUpdatedTs, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
      .map(enrollmentMapping -> enrollmentMapping.getCourseSummary().getTrainingId())
      .findFirst()
      .flatMap(courseRepository::findById);
  }

  public CourseSummary search(Long courseId) {
    try{
      return courseRepository.findById(courseId)
          .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
    }catch(ResourceNotFoundException ex){
      throw ex;
    }catch(Exception ex){
      log.error("Database error while fetching course", ex);
      throw new DatabaseException("Failed to fetch course with id: " + courseId, ex);
    }
  }

  public List<CourseCardDetailDto> getCourseCardList(String viewType, String category) {
    try{
      if(StringUtils.isNotBlank(viewType)){
        List<Object[]> courseCardDetailList = null;
        log.info("viewType :: {}",viewType);
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
            return List.of();
        }
        return courseCardDetailList != null ? courseCardDetailList.stream()
          .map(row -> new CourseCardDetailDto((Long) row[0],(String) row[1], (String) row[2], (String) row[3],(Long) row[4], (String) row[5], (Long) row[6], (BigDecimal) row[7], (String) row[8]))
          .collect(Collectors.toList()): List.of();
      }
      return List.of();
    }catch(Exception ex){
      log.error("Database error while fetching course card list", ex);
      throw new DatabaseException("Failed to fetch course card list", ex);
    }
  }
}
