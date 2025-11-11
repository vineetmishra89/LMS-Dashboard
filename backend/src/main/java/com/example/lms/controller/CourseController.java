package com.example.lms.controller;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.CourseCardDetailDto;
import com.example.lms.dto.FileNode;
import com.example.lms.dto.FolderNode;
import com.example.lms.service.CourseService;
import com.example.lms.service.UserTokenSharePointService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  
  public CourseController(CourseService courseService, UserTokenSharePointService userTokenSharePointService) { 
    this.courseService = courseService;
    this.userTokenSharePointService = userTokenSharePointService;
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
    CourseSummary summary = courseService.search(courseId);
    return summary;
  }

  @GetMapping("/courseCard/viewType/{viewType}")
  public List<CourseCardDetailDto> getCourseCardList(@PathVariable(required = true) String viewType, @RequestParam(required = false) String category) {
    return courseService.getCourseCardList(viewType,category);
  }

  @GetMapping("/materialCourse")
  public ResponseEntity<?> getMaterialCourseLink(@RequestParam Long trngId, @RequestParam String userId, HttpServletRequest httpRequest) {
    try {
      logger.info("Received request to get material course link for trngId: {} and userId: {}", trngId, userId);
      
      String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
      if (authorization == null || !authorization.startsWith("Bearer ")) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid Authorization header");
        error.put("message", "Authorization header must start with 'Bearer '");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
      }
      
      String bearerToken = authorization.substring(7).trim();
      
      String folderPath = courseService.getFolderPathByTrainingId(trngId);
      
      if (folderPath == null || folderPath.isEmpty()) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Folder path not found");
        error.put("message", "No folder path configured for training ID: " + trngId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }
      
      logger.info("Found folder path (webUrl): {} for trngId: {}", folderPath, trngId);
      
      List<String> allFilePaths = userTokenSharePointService.fetchAllFilePathsFromWebUrl(bearerToken, folderPath);
      
      List<String> nonVideoFilePaths = filterNonVideoFilePaths(allFilePaths);
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("trngId", trngId);
      response.put("userId", userId);
      response.put("folderPath", folderPath);
      response.put("fileCount", nonVideoFilePaths.size());
      response.put("filePaths", nonVideoFilePaths);
      
      logger.info("Successfully retrieved {} non-video file paths for trngId: {}", nonVideoFilePaths.size(), trngId);
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      logger.error("Error getting material course link for trngId: {} and userId: {}: {}", trngId, userId, e.getMessage(), e);
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
   * Filters non-video file paths from the list of file paths.
   * Video files are identified by extensions in the file path.
   */
  private List<String> filterNonVideoFilePaths(List<String> filePaths) {
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
   * Checks if a file path is a video file based on its extension.
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
