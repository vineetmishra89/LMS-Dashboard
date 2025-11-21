package com.example.lms.service.impl;

import com.example.lms.constants.RoleConstants;
import com.example.lms.domain.TrainingPlanSummary;
import com.example.lms.dto.TrainingPlanRequestDTO;
import com.example.lms.dto.TrainingPlanResponseDTO;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.repo.TrainingPlanSummaryRepository;
import com.example.lms.service.TrainingPlanService;
import com.example.lms.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Implementation of TrainingPlanService.
 * Handles training plan creation with role validation and duplicate checking.
 */
@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanServiceImpl.class);
    
    private final TrainingPlanSummaryRepository trainingPlanRepository;
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final UserRoleService userRoleService;
    
    public TrainingPlanServiceImpl(TrainingPlanSummaryRepository trainingPlanRepository,
                                  EmployeeDetailsRepository employeeDetailsRepository,
                                  UserRoleService userRoleService) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.userRoleService = userRoleService;
    }
    
    @Override
    @Transactional
    public TrainingPlanResponseDTO addTrainingPlan(TrainingPlanRequestDTO request) {
        logger.info("Adding training plan: {} for user: {}", request.getTrainingPlanName(), request.getEmailId());
        
        String trainingPlanName = request.getTrainingPlanName();
        String emailId = request.getEmailId();
        
        if (trainingPlanName == null || trainingPlanName.trim().isEmpty()) {
            throw new ValidationException("Training plan name is required");
        }
        
        if (emailId == null || emailId.trim().isEmpty()) {
            throw new ValidationException("Email ID is required");
        }
        
        if (!employeeDetailsRepository.existsByEmailIdIgnoreCase(emailId)) {
            throw new ResourceNotFoundException("User with email " + emailId + " not found in LMS_EMPLOYEE_DTLS");
        }
        
        validateUserRoles(emailId);
        
        String finalTrainingPlanName = handleDuplicateTrainingPlanName(trainingPlanName, emailId);
        
        OffsetDateTime currentTime = OffsetDateTime.now();
        
        TrainingPlanSummary trainingPlan = TrainingPlanSummary.builder()
                .trngPlanName(finalTrainingPlanName)
                .emailId(emailId)
                .status("DRAFT")
                .createdBy(emailId)
                .createdTs(currentTime)
                .updatedBy(emailId)
                .updatedTs(currentTime)
                .build();
        
        TrainingPlanSummary savedPlan = trainingPlanRepository.save(trainingPlan);
        
        logger.info("Successfully created training plan with ID: {} for user: {}", 
                   savedPlan.getTrngPlanId(), emailId);
        
        return TrainingPlanResponseDTO.builder()
                .trainingPlanId(savedPlan.getTrngPlanId())
                .trainingPlanName(savedPlan.getTrngPlanName())
                .emailId(savedPlan.getEmailId())
                .status(savedPlan.getStatus())
                .createdTs(savedPlan.getCreatedTs())
                .message("Training plan created successfully")
                .build();
    }
    
    /**
     * Validate that the user has one of the required roles.
     * Required roles: ROLE_RO, ROLE_ADM, ROLE_PROJECT_ADMIN, ROLE_LND_ADMIN
     * 
     * @param emailId User's email ID
     * @throws ValidationException if user doesn't have any of the required roles
     */
    private void validateUserRoles(String emailId) {
        logger.debug("Validating roles for user: {}", emailId);
        
        boolean hasRequiredRole = userRoleService.hasRole(emailId, RoleConstants.ROLE_RO) ||
                                 userRoleService.hasRole(emailId, RoleConstants.ROLE_PROJECT_ADMIN) ||
                                 userRoleService.hasRole(emailId, RoleConstants.ROLE_LND_ADMIN);
        
        if (!hasRequiredRole) {
            logger.warn("User {} does not have required roles for creating training plan", emailId);
            throw new ValidationException(
                "User does not have required roles. Required roles: ROLE_RO, ROLE_PROJECT_ADMIN, or ROLE_LND_ADMIN"
            );
        }
        
        logger.debug("User {} has required roles", emailId);
    }
    
    /**
     * Handle duplicate training plan names according to business rules:
     * - If training plan exists for the same user, throw exception
     * - If training plan exists for a different user, append email prefix to the name
     * 
     * @param trainingPlanName Original training plan name
     * @param emailId User's email ID
     * @return Final training plan name (possibly modified)
     * @throws DuplicateResourceException if training plan already exists for the same user
     */
    private String handleDuplicateTrainingPlanName(String trainingPlanName, String emailId) {
        logger.debug("Checking for duplicate training plan: {} for user: {}", trainingPlanName, emailId);
        
        Optional<TrainingPlanSummary> existingPlanForUser = 
            trainingPlanRepository.findByTrainingPlanNameAndEmailId(trainingPlanName, emailId);
        
        if (existingPlanForUser.isPresent()) {
            logger.warn("Training plan {} already exists for user {}", trainingPlanName, emailId);
            throw new DuplicateResourceException(
                "Training Plan " + trainingPlanName + " already exists for the user " + emailId + "."
            );
        }
        
        boolean existsForOtherUser = trainingPlanRepository.existsByTrainingPlanName(trainingPlanName);
        
        if (existsForOtherUser) {
            String emailPrefix = emailId.substring(0, emailId.indexOf('@'));
            String modifiedName = trainingPlanName + "_" + emailPrefix;
            logger.info("Training plan {} exists for another user, using modified name: {}", 
                       trainingPlanName, modifiedName);
            return modifiedName;
        }
        
        return trainingPlanName;
    }
}
