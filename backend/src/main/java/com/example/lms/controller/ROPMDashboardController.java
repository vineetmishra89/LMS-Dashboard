package com.example.lms.controller;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.ROPMDashboardEmployeesRequest;
import com.example.lms.service.ROPMDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ro-pm-dashboard")
@CrossOrigin
@Tag(name = "RO/PM Dashboard", description = "APIs for RO/PM Dashboard filters and employee retrieval")
public class ROPMDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(ROPMDashboardController.class);
    
    private final ROPMDashboardService roPmDashboardService;
    
    public ROPMDashboardController(ROPMDashboardService roPmDashboardService) {
        this.roPmDashboardService = roPmDashboardService;
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
}
