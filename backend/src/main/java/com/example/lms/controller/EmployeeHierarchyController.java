package com.example.lms.controller;

import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.service.EmployeeHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee-hierarchy")
@CrossOrigin
public class EmployeeHierarchyController {

  private static final Logger logger = LoggerFactory.getLogger(EmployeeHierarchyController.class);

  private final EmployeeHierarchyService employeeHierarchyService;

  public EmployeeHierarchyController(EmployeeHierarchyService employeeHierarchyService) {
    this.employeeHierarchyService = employeeHierarchyService;
  }

  @GetMapping
  public ResponseEntity<EmployeeHierarchyResponseDto> getEmployeeHierarchy(
    @RequestParam(required = true) String userId) {
    
    logger.info("Received request to fetch employee hierarchy for userId: {}", userId);
    
    try {
      EmployeeHierarchyResponseDto response = employeeHierarchyService.getEmployeeHierarchy(userId);
      logger.info("Successfully fetched employee hierarchy for userId: {}. Total employees: {}", 
        userId, response.totalEmployees());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      logger.error("Error processing employee hierarchy request for userId: {}", userId, e);
      throw e;
    }
  }
}
