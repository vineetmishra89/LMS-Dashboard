package com.example.lms.controller;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.dto.MetricsRequestDto;
import com.example.lms.dto.MetricsResponseDto;
import com.example.lms.dto.ROPMDashboardEmployeesRequest;
import com.example.lms.service.EmployeeHierarchyService;
import com.example.lms.service.MetricsExcelExportService;
import com.example.lms.service.MetricsService;
import com.example.lms.service.ROPMDashboardService;
import com.example.lms.util.JwtClaimExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/ro-pm-dashboard")
@CrossOrigin
@Tag(name = "RO/PM Dashboard", description = "APIs for RO/PM Dashboard filters and employee retrieval")
public class ROPMDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(ROPMDashboardController.class);
    
    private final ROPMDashboardService roPmDashboardService;
    private final MetricsService metricsService;
    private final EmployeeHierarchyService employeeHierarchyService;
    private final MetricsExcelExportService excelExportService;
    
    public ROPMDashboardController(ROPMDashboardService roPmDashboardService,
                                    MetricsService metricsService,
                                    EmployeeHierarchyService employeeHierarchyService,
                                    MetricsExcelExportService excelExportService) {
        this.roPmDashboardService = roPmDashboardService;
        this.metricsService = metricsService;
        this.employeeHierarchyService = employeeHierarchyService;
        this.excelExportService = excelExportService;
    }
    
    @GetMapping("/filters/sbus")
    @Operation(
        summary = "Get SBU filter options",
        description = "Returns distinct SBU values for projects where user has any project role (PM, ADM, Offshore DD)"
    )
    public ResponseEntity<List<String>> getSbus(@RequestParam String userId) {
        logger.info("Received request to get SBUs for user: {}", userId);
        
        try {
            List<String> sbus = roPmDashboardService.getSbusByUser(userId);
            logger.info("Returning {} SBUs for user: {}", sbus.size(), userId);
            return ResponseEntity.ok(sbus);
        } catch (Exception e) {
            logger.error("Error fetching SBUs for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/filters/projects")
    @Operation(
        summary = "Get Project filter options",
        description = "Returns distinct project names where user has any project role, optionally filtered by SBUs"
    )
    public ResponseEntity<List<String>> getProjects(
            @RequestParam String userId,
            @RequestParam(required = false) List<String> sbus) {
        logger.info("Received request to get projects for user: {} with SBUs: {}", userId, sbus);
        
        try {
            List<String> projects = roPmDashboardService.getProjectsByUserAndSbus(userId, sbus);
            logger.info("Returning {} projects for user: {}", projects.size(), userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Error fetching projects for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/employees")
    @Operation(
        summary = "Get employees for RO/PM Dashboard",
        description = "Returns union of employees from selected projects and RO hierarchy. " +
                     "If no projects specified, returns only RO hierarchy employees."
    )
    public ResponseEntity<List<EmployeeDetailsDto>> getEmployees(
            @RequestBody ROPMDashboardEmployeesRequest request) {
        logger.info("Received request to get employees for RO: {} with projects: {}", 
                   request.getRoEmailId(), request.getProjects());
        
        try {
            List<EmployeeDetailsDto> employees = roPmDashboardService.getEmployeesForROPMDashboard(
                request.getRoEmailId(), 
                request.getProjects()
            );
            logger.info("Returning {} employees for RO/PM Dashboard", employees.size());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            logger.error("Error fetching employees for RO/PM Dashboard", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    @PostMapping("/metrics/report")
    @Operation(
        summary = "Get metrics report for RO/PM Dashboard",
        description = "Returns metrics filtered to show only employees in the logged-in user's reporting hierarchy"
    )
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
    
    @GetMapping("/metrics/report")
    @Operation(
        summary = "Get metrics report by query parameters",
        description = "Returns metrics filtered to show only employees in the logged-in user's reporting hierarchy"
    )
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
    
    @GetMapping("/metrics/report/download")
    @Operation(
        summary = "Download metrics report as Excel",
        description = "Downloads metrics report filtered to show only employees in the logged-in user's reporting hierarchy"
    )
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
            logger.error("Unable to extract email from JWT token - no valid email claim found");
            throw new IllegalArgumentException("JWT token does not contain a valid email claim");
        }
        
        if (!email.contains("@") || email.contains(" ")) {
            logger.error("Extracted value '{}' from JWT is not a valid email address", email);
            throw new IllegalArgumentException("JWT token contains invalid email format: " + email);
        }
        
        logger.debug("Successfully extracted and validated email from JWT: {}", email);
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
