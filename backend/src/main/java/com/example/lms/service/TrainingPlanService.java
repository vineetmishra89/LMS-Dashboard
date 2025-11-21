package com.example.lms.service;

import com.example.lms.dto.TrainingPlanRequestDTO;
import com.example.lms.dto.TrainingPlanResponseDTO;

/**
 * Service interface for training plan operations.
 * Provides methods to create and manage training plans.
 */
public interface TrainingPlanService {
    
    /**
     * Add a new training plan.
     * Validates user roles and checks for duplicate training plan names.
     * 
     * @param request Training plan request containing plan name and email ID
     * @return TrainingPlanResponseDTO containing the created training plan details
     * @throws com.example.lms.exception.ValidationException if user doesn't have required roles
     * @throws com.example.lms.exception.DuplicateResourceException if training plan already exists for the user
     * @throws com.example.lms.exception.ResourceNotFoundException if user not found in LMS_EMPLOYEE_DTLS
     */
    TrainingPlanResponseDTO addTrainingPlan(TrainingPlanRequestDTO request);
}
