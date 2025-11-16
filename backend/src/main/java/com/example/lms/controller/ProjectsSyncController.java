package com.example.lms.controller;

import com.example.lms.dto.ProjectExcelSyncResult;
import com.example.lms.service.ProjectExcelSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
@Tag(name = "Projects Sync", description = "APIs for syncing project data from Excel")
public class ProjectsSyncController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectsSyncController.class);
    
    @Autowired
    private ProjectExcelSyncService projectExcelSyncService;
    
    @PostMapping("/sync-from-excel")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Sync projects from Excel file",
        description = "Reads project data from Excel file (configured in application.properties), " +
                     "looks up PM and ADM emails via Graph API, inserts projects into LMS_PROJECT_DTLS, " +
                     "and updates employee project assignments in LMS_EMPLOYEE_DTLS. " +
                     "Errors are logged to Column L in the Excel file."
    )
    public ResponseEntity<?> syncProjectsFromExcel(HttpServletRequest request) {
        
        logger.info("Received request to sync projects from Excel file");
        
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid Authorization header");
                error.put("message", "Authorization header must start with 'Bearer '");
                logger.error("Invalid Authorization header in sync request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String bearerToken = authorization.substring(7).trim();
            
            ProjectExcelSyncResult result = projectExcelSyncService.syncFromExcel(bearerToken);
            
            logger.info("Project sync completed. Total rows: {}, Projects inserted: {}, Projects skipped: {}, " +
                       "Employees updated: {}, Graph lookups succeeded: {}, Graph lookups failed: {}, Errors: {}",
                    result.getTotalRows(), result.getProjectsInserted(), result.getProjectsSkippedExisting(),
                    result.getEmployeesUpdated(), result.getGraphLookupsSucceeded(), result.getGraphLookupsFailed(),
                    result.getErrors().size());
            
            if (!result.getErrors().isEmpty() && result.getTotalRows() == 0) {
                return ResponseEntity.badRequest().body(result);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error syncing projects from Excel: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sync failed");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
