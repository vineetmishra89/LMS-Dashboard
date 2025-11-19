package com.example.lms.controller;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.TrainingSearchHistory;
import com.example.lms.dto.CourseCardDetailDto;
import com.example.lms.dto.FileNode;
import com.example.lms.dto.FolderNode;
import com.example.lms.repo.TrainingSearchHistoryRepository;
import com.example.lms.service.CourseService;
import com.example.lms.service.UserTokenSharePointService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {
  private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
  private final CourseService courseService;
  private final UserTokenSharePointService userTokenSharePointService;
  private final TrainingSearchHistoryRepository trainingSearchHistoryRepository;

  public CourseController(CourseService courseService, UserTokenSharePointService userTokenSharePointService, TrainingSearchHistoryRepository trainingSearchHistoryRepository) {
    this.courseService = courseService;
    this.userTokenSharePointService = userTokenSharePointService;
    this.trainingSearchHistoryRepository = trainingSearchHistoryRepository;
  }

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
    CourseSummary summary = courseService.search(courseId,userId);
    return summary;
  }

  @GetMapping("/courseCard/viewType/{viewType}")
  public List<CourseCardDetailDto> getCourseCardList(@PathVariable(required = true) String viewType, @RequestParam(required = false) String category) {
    return courseService.getCourseCardList(viewType,category);
  }

  @GetMapping("/materialCourse")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<?> getMaterialCourseLink(@RequestParam Long trngId, HttpServletRequest httpRequest) {
    try {
      logger.info("Received request to get material course link for trngId: {}", trngId);
      String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
      if (authorization == null || !authorization.startsWith("Bearer ")) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid Authorization header");
        error.put("message", "Authorization header must start with 'Bearer '");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
      }
      String folderPath = courseService.getFolderPathByTrainingId(trngId);

      if (folderPath == null || folderPath.isEmpty()) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Folder path not found");
        error.put("message", "No folder path configured for training ID: " + trngId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }
      Map<String, Object> response = courseService.getMaterialCourseLink(trngId, authorization, folderPath);


      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Error getting material course link for trngId: {}: {}", trngId, e.getMessage(), e);
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to get material course link");
      error.put("message", e.getMessage());

      if (e.getMessage() != null &&
          (e.getMessage().contains("401") ||
           e.getMessage().contains("Unauthorized") ||
           e.getMessage().contains("Invalid token"))) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
      }

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Recursively filters non-video files from the folder structure.
   * Video files are identified by extensions: .mp4, .avi, .mov, .wmv, .flv, .mkv, .webm
   */

  @PostMapping("/recordSearchHistory")
  public ResponseEntity<?> recordSearchHistory(@RequestBody Map<String, Object> request) {
    try {
      logger.info("Received request to record search history: {}", request);

      Object trngIdObj = request.get("trng_id");
      String emailId = (String) request.get("email_id");

      if (trngIdObj == null || emailId == null || emailId.isEmpty()) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Missing required fields");
        error.put("message", "Both trng_id and email_id are required");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
      }

      Integer trngId;
      if (trngIdObj instanceof Integer) {
        trngId = (Integer) trngIdObj;
      } else if (trngIdObj instanceof Long) {
        trngId = ((Long) trngIdObj).intValue();
      } else if (trngIdObj instanceof String) {
        try {
          trngId = Integer.parseInt((String) trngIdObj);
        } catch (NumberFormatException e) {
          Map<String, String> error = new HashMap<>();
          error.put("error", "Invalid trng_id format");
          error.put("message", "trng_id must be a valid integer");
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
      } else {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid trng_id type");
        error.put("message", "trng_id must be a number");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
      }

      OffsetDateTime currentTime = OffsetDateTime.now();

      TrainingSearchHistory searchHistory = new TrainingSearchHistory();
      searchHistory.setTrngId(trngId);
      searchHistory.setEmailId(emailId);
      searchHistory.setViewTs(currentTime);
      searchHistory.setCreatedBy(emailId);
      searchHistory.setCreatedTs(currentTime);
      searchHistory.setUpdatedBy(emailId);
      searchHistory.setUpdatedTs(currentTime);

      trainingSearchHistoryRepository.save(searchHistory);

      logger.info("Successfully recorded search history for trngId: {} and emailId: {}", trngId, emailId);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Search history recorded successfully");
      response.put("trng_id", trngId);
      response.put("email_id", emailId);
      response.put("view_ts", currentTime);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Error recording search history: {}", e.getMessage(), e);
      Map<String, String> error = new HashMap<>();
      error.put("error", "Failed to record search history");
      error.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

}
