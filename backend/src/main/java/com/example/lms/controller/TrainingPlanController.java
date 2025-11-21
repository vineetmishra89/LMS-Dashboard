package com.example.lms.controller;

import com.example.lms.dto.AddTraineesToPlanRequestDTO;
import com.example.lms.dto.AddTraineesToPlanResponseDTO;
import com.example.lms.dto.TrainingPlanRequestDTO;
import com.example.lms.dto.TrainingPlanResponseDTO;
import com.example.lms.service.TrainingPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for training plan operations.
 * Provides endpoints to create and manage training plans.
 */
@RestController
@RequestMapping("/api/training-plans")
@CrossOrigin
public class TrainingPlanController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanController.class);
    
    private final TrainingPlanService trainingPlanService;
    
    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }
    
    /**
     * Add a new training plan.
     * 
     * Validates that the user has one of the required roles:
     * - ROLE_RO
     * - ROLE_PROJECT_ADMIN
     * - ROLE_LND_ADMIN
     * 
     * Handles duplicate training plan names:
     * - If training plan exists for the same user, returns error
     * - If training plan exists for a different user, appends email prefix to the name
     * 
     * @param request Training plan request containing plan name and email ID
     * @return ResponseEntity with TrainingPlanResponseDTO or error message
     */
    @PostMapping("/add")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> addTrainingPlan(@RequestBody TrainingPlanRequestDTO request) {
        try {
            logger.info("Received request to add training plan: {} for user: {}", 
                       request.getTrainingPlanName(), request.getEmailId());
            
            TrainingPlanResponseDTO response = trainingPlanService.addTrainingPlan(request);
            
            logger.info("Successfully added training plan with ID: {} for user: {}", 
                       response.getTrainingPlanId(), response.getEmailId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error adding training plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add training plan", e.getMessage()));
        }
    }
    
    /**
     * Add trainees to an existing training plan.
     * 
     * Creates enrollment mappings and enrollment details for the specified trainees and trainings.
     * Validates that:
     * - Training plan exists and status is DRAFT
     * - All training IDs exist
     * - All email IDs exist in LMS_EMPLOYEE_DTLS
     * 
     * @param request Request containing training plan ID, training IDs, and email IDs
     * @return ResponseEntity with AddTraineesToPlanResponseDTO or error message
     */
    @PostMapping("/add-trainees")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> addTraineesToPlan(@RequestBody AddTraineesToPlanRequestDTO request) {
        try {
            logger.info("Received request to add trainees to training plan: {}", request.getTrainingPlanId());
            
            AddTraineesToPlanResponseDTO response = trainingPlanService.addTraineesToPlan(request);
            
            logger.info("Successfully added {} enrollments to training plan {}", 
                       response.getEnrollmentsCreated(), response.getTrainingPlanId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error adding trainees to training plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add trainees to training plan", e.getMessage()));
        }
    }
    
    /**
     * Error response DTO for API errors.
     */
    private static class ErrorResponse {
        private final String error;
        private final String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
