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
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  private final UserTokenSharePointService userTokenSharePointService;
  @Getter
  @Value("${course.interval}")
  private String courseInterval = null;

  public CourseService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, CourseCardRepository courseCardRepository, UserTokenSharePointService userTokenSharePointService) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseCardRepository = courseCardRepository;
    this.userTokenSharePointService = userTokenSharePointService;
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

  public CourseSummary search(Long courseId, String userId) {
    CourseSummary courseSummary= null;
    try{
      EnrollmentMapping enrollmentMapping = enrollmentRepository.getEnrollmentMappingByUserIdAndTrainingId(userId,courseId);
      courseSummary = courseRepository.findCourseSummaryByTrainingId(courseId);
      if(enrollmentMapping!=null){
        List<EnrollmentMapping> enrollmentMappings = new ArrayList<>();
        enrollmentMappings.add(enrollmentMapping);
        courseSummary.setEnrollmentMappings(enrollmentMappings);
      }
    }catch(ResourceNotFoundException ex){
      throw ex;
    }catch(Exception ex){
      log.error("Database error while fetching course", ex);
      throw new DatabaseException("Failed to fetch course with id: " + courseId, ex);
    }
    return courseSummary;
  }

  public List<CourseCardDetailDto> getCourseCardList(String viewType, String category) {
    try{
      if(StringUtils.isNotBlank(viewType)){
        List<Object[]> courseCardDetailList = null;
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
          .map(row -> new CourseCardDetailDto(
            toLong(row[0]),
            (String) row[1],
            (String) row[2],
            (String) row[3],
            toLong(row[4]),
            (String) row[5],
            toLong(row[6]),
            (BigDecimal) row[7],
            (String) row[8]))
          .collect(Collectors.toList()): List.of();
      }
      return List.of();
    }catch(Exception ex){
      log.error("Database error while fetching course card list", ex);
      throw new DatabaseException("Failed to fetch course card list", ex);
    }
  }

  private static long toLong(Object obj) {
    if (obj == null) {
      return 0L;
    }
    return ((Number) obj).longValue();
  }

  public String getFolderPathByTrainingId(Long trainingId) {
    try {
      CourseSummary course = courseRepository.findById(trainingId)
        .orElseThrow(() -> new ResourceNotFoundException("Course", "id", trainingId));
      return course.getFolderPath();
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Database error while fetching folder path for training id: {}", trainingId, ex);
      throw new DatabaseException("Failed to fetch folder path for training id: " + trainingId, ex);
    }
  }

  public Map<String, Object> getMaterialCourseLink(Long trngId, String authorization, String folderPath){

    String bearerToken = authorization.substring(7).trim();



    log.info("Found folder path: {} for trngId: {}", folderPath, trngId);

    List<String> allFilePaths = userTokenSharePointService.fetchAllFilePathsFromWebUrl(bearerToken,folderPath);

    List<String> nonVideoFiles = filterNonVideoFiles(allFilePaths);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("trngId", trngId);
    response.put("folderPath", folderPath);
    response.put("fileCount", nonVideoFiles.size());
    response.put("files", nonVideoFiles);
    log.info("Successfully retrieved {} non-video files for trngId: {}", nonVideoFiles.size(), trngId);
    return response;

  }
  private List<String> filterNonVideoFiles(List<String> filePaths) {
    List<String> nonVideoFilePaths = new ArrayList<>();

    if (filePaths == null) {
      return nonVideoFilePaths;
    }

    for (String filePath : filePaths) {
      if (!isVideoFilePath(filePath)) {
        nonVideoFilePaths.add(filePath);
      }
    }

    return nonVideoFilePaths;
  }

  /**
   * Checks if a file is a video file based on its extension.
   */
  private boolean isVideoFilePath(String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      return false;
    }

    String lowerCaseFilePath = filePath.toLowerCase();
    return lowerCaseFilePath.endsWith(".mp4") ||
      lowerCaseFilePath.endsWith(".avi") ||
      lowerCaseFilePath.endsWith(".mov") ||
      lowerCaseFilePath.endsWith(".wmv") ||
      lowerCaseFilePath.endsWith(".flv") ||
      lowerCaseFilePath.endsWith(".mkv") ||
      lowerCaseFilePath.endsWith(".webm") ||
      lowerCaseFilePath.endsWith(".m4v") ||
      lowerCaseFilePath.endsWith(".mpg") ||
      lowerCaseFilePath.endsWith(".mpeg");
  }
}
