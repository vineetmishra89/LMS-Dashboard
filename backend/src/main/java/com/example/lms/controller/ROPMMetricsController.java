package com.example.lms.controller;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.dto.MetricsRequestDto;
import com.example.lms.dto.MetricsResponseDto;
import com.example.lms.service.EmployeeHierarchyService;
import com.example.lms.service.MetricsExcelExportService;
import com.example.lms.service.MetricsService;
import com.example.lms.util.JwtClaimExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ro-pm/metrics")
@CrossOrigin
public class ROPMMetricsController {

  private static final Logger logger = LoggerFactory.getLogger(ROPMMetricsController.class);

  private final MetricsService metricsService;
  private final EmployeeHierarchyService employeeHierarchyService;
  private final MetricsExcelExportService excelExportService;

  public ROPMMetricsController(MetricsService metricsService,
                                EmployeeHierarchyService employeeHierarchyService,
                                MetricsExcelExportService excelExportService) {
    this.metricsService = metricsService;
    this.employeeHierarchyService = employeeHierarchyService;
    this.excelExportService = excelExportService;
  }

  @PostMapping("/report")
  public MetricsResponseDto getMetricsReport(
      @RequestBody MetricsRequestDto request,
      @RequestHeader("Authorization") String authHeader) {
    
    logger.info("RO/PM Metrics report requested");
    
    String currentUserEmail = extractEmailFromToken(authHeader);
    logger.info("Current user email: {}", currentUserEmail);
    
    List<String> employeeEmails = getEmployeeEmailsInHierarchy(currentUserEmail);
    logger.info("Found {} employees in hierarchy for user: {}", employeeEmails.size(), currentUserEmail);
    
    return metricsService.getMetricsForEmployees(request, employeeEmails);
  }

  @GetMapping("/report")
  public MetricsResponseDto getMetricsReportByParams(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String technology,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false, defaultValue = "10") Integer topN,
      @RequestHeader("Authorization") String authHeader) {
    
    logger.info("RO/PM Metrics report requested via GET");
    
    MetricsRequestDto request = new MetricsRequestDto();
    request.setCategory(category);
    request.setLevel(level);
    request.setTechnology(technology);
    request.setStartDate(startDate);
    request.setEndDate(endDate);
    request.setTopN(topN);
    
    String currentUserEmail = extractEmailFromToken(authHeader);
    logger.info("Current user email: {}", currentUserEmail);
    
    List<String> employeeEmails = getEmployeeEmailsInHierarchy(currentUserEmail);
    logger.info("Found {} employees in hierarchy for user: {}", employeeEmails.size(), currentUserEmail);
    
    return metricsService.getMetricsForEmployees(request, employeeEmails);
  }

  @GetMapping("/report/download")
  public ResponseEntity<byte[]> downloadMetricsReport(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String technology,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false, defaultValue = "10") Integer topN,
      @RequestHeader("Authorization") String authHeader) throws IOException {
    
    logger.info("RO/PM Metrics report download requested");
    
    MetricsRequestDto request = new MetricsRequestDto();
    request.setCategory(category);
    request.setLevel(level);
    request.setTechnology(technology);
    request.setStartDate(startDate);
    request.setEndDate(endDate);
    request.setTopN(topN);
    
    String currentUserEmail = extractEmailFromToken(authHeader);
    logger.info("Current user email: {}", currentUserEmail);
    
    List<String> employeeEmails = getEmployeeEmailsInHierarchy(currentUserEmail);
    logger.info("Found {} employees in hierarchy for user: {}", employeeEmails.size(), currentUserEmail);
    
    MetricsResponseDto metricsData = metricsService.getMetricsForEmployees(request, employeeEmails);
    byte[] excelBytes = excelExportService.generateMetricsExcel(metricsData);
    
    String filename = generateFilename(startDate, endDate);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(excelBytes.length);
    
    return ResponseEntity.ok()
      .headers(headers)
      .body(excelBytes);
  }

  private String extractEmailFromToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      logger.error("Invalid or missing Authorization header");
      throw new IllegalArgumentException("Invalid or missing Authorization header");
    }
    
    String token = authHeader.substring(7); // Remove "Bearer " prefix
    String email = JwtClaimExtractor.extractUsername(token);
    
    if (email == null || email.trim().isEmpty()) {
      logger.error("Unable to extract email from JWT token");
      throw new IllegalArgumentException("Unable to extract email from JWT token");
    }
    
    return email;
  }

  private List<String> getEmployeeEmailsInHierarchy(String roEmailId) {
    try {
      EmployeeHierarchyResponseDto hierarchy = employeeHierarchyService.getEmployeeHierarchy(roEmailId);
      
      List<String> employeeEmails = hierarchy.employees().stream()
        .map(EmployeeDetailsDto::emailId)
        .collect(Collectors.toList());
      
      logger.info("Successfully retrieved {} employee emails from hierarchy", employeeEmails.size());
      return employeeEmails;
      
    } catch (Exception e) {
      logger.error("Error fetching employee hierarchy for user: {}", roEmailId, e);
      logger.warn("Returning empty employee list for user: {}", roEmailId);
      return List.of();
    }
  }

  private String generateFilename(LocalDate startDate, LocalDate endDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    StringBuilder filename = new StringBuilder("hierarchy_metrics_report");
    
    if (startDate != null) {
      filename.append("_from_").append(startDate.format(formatter));
    }
    if (endDate != null) {
      filename.append("_to_").append(endDate.format(formatter));
    }
    
    filename.append(".xlsx");
    return filename.toString();
  }
}
