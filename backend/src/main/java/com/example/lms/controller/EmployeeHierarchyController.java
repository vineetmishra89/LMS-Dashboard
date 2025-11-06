package com.example.lms.controller;

import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.dto.EmployeeSyncResult;
import com.example.lms.service.EmployeeGraphSyncService;
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
  private final EmployeeGraphSyncService employeeGraphSyncService;

  public EmployeeHierarchyController(EmployeeHierarchyService employeeHierarchyService,
                                    EmployeeGraphSyncService employeeGraphSyncService) {
    this.employeeHierarchyService = employeeHierarchyService;
    this.employeeGraphSyncService = employeeGraphSyncService;
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
  
  @PostMapping("/sync-from-graph")
  public ResponseEntity<?> syncEmployeeHierarchyFromGraph(@RequestParam String emailId) {
    
    logger.info("Received request to sync employee hierarchy from Graph API for emailId: {}", emailId);
    
    try {
      if (emailId == null || emailId.trim().isEmpty()) {
        return ResponseEntity.badRequest().body("emailId parameter is required");
      }
      
      EmployeeSyncResult result = employeeGraphSyncService.syncEmployeeHierarchyFromGraph(emailId.trim());
      
      logger.info("Employee hierarchy sync completed for emailId: {}. Inserted: {}, Updated: {}, Total: {}", 
              emailId, result.getInsertedCount(), result.getUpdatedCount(), result.getTotalProcessed());
      
      if (!result.getErrors().isEmpty() && result.getTotalProcessed() == 0) {
        return ResponseEntity.badRequest().body(result);
      }
      
      return ResponseEntity.ok(result);
      
    } catch (Exception e) {
      logger.error("Error syncing employee hierarchy from Graph API for emailId: {}", emailId, e);
      return ResponseEntity.internalServerError()
              .body("Failed to sync employee hierarchy: " + e.getMessage());
    }
  }
}
