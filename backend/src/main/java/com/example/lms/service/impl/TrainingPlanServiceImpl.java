package com.example.lms.service.impl;

import com.example.lms.constants.RoleConstants;
import com.example.lms.constants.TrainingPlanStatus;
import com.example.lms.domain.CourseSummary;
import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentMapping;
import com.example.lms.domain.TrainingPlanSummary;
import com.example.lms.dto.AddTraineesToPlanRequestDTO;
import com.example.lms.dto.AddTraineesToPlanResponseDTO;
import com.example.lms.dto.TrainingPlanRequestDTO;
import com.example.lms.dto.TrainingPlanResponseDTO;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.CourseDetailRepository;
import com.example.lms.repo.CourseRepository;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.repo.TrainingPlanSummaryRepository;
import com.example.lms.service.TrainingPlanService;
import com.example.lms.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseDetailRepository courseDetailRepository;
    
    public TrainingPlanServiceImpl(TrainingPlanSummaryRepository trainingPlanRepository,
                                  EmployeeDetailsRepository employeeDetailsRepository,
                                  UserRoleService userRoleService,
                                  EnrollmentRepository enrollmentRepository,
                                  CourseRepository courseRepository,
                                  CourseDetailRepository courseDetailRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.userRoleService = userRoleService;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.courseDetailRepository = courseDetailRepository;
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
                .status(TrainingPlanStatus.DRAFT)
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
    
    @Override
    @Transactional
    public AddTraineesToPlanResponseDTO addTraineesToPlan(AddTraineesToPlanRequestDTO request) {
        logger.info("Adding trainees to training plan: {}", request.getTrainingPlanId());
        
        Long trainingPlanId = request.getTrainingPlanId();
        List<Long> trainingIds = request.getTrainingIds();
        List<String> emailIds = request.getEmailIds();
        
        if (trainingPlanId == null) {
            throw new ValidationException("Training plan ID is required");
        }
        
        if (trainingIds == null || trainingIds.isEmpty()) {
            throw new ValidationException("At least one training ID is required");
        }
        
        if (emailIds == null || emailIds.isEmpty()) {
            throw new ValidationException("At least one email ID is required");
        }
        
        TrainingPlanSummary trainingPlan = trainingPlanRepository.findById(trainingPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Training plan with ID " + trainingPlanId + " not found"));
        
        if (trainingPlan.getStatus() != TrainingPlanStatus.DRAFT) {
            throw new ValidationException("Training plan status must be DRAFT to add trainees. Current status: " + trainingPlan.getStatus());
        }
        
        for (String emailId : emailIds) {
            if (!employeeDetailsRepository.existsByEmailIdIgnoreCase(emailId)) {
                throw new ResourceNotFoundException("User with email " + emailId + " not found in LMS_EMPLOYEE_DTLS");
            }
        }
        
        for (Long trainingId : trainingIds) {
            if (!courseRepository.existsById(trainingId)) {
                throw new ResourceNotFoundException("Training with ID " + trainingId + " not found");
            }
        }
        
        OffsetDateTime currentTime = OffsetDateTime.now();
        int enrollmentsCreated = 0;
        
        for (String emailId : emailIds) {
            for (Long trainingId : trainingIds) {
                CourseSummary training = courseRepository.findById(trainingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Training with ID " + trainingId + " not found"));
                
                EnrollmentMapping enrollment = new EnrollmentMapping();
                enrollment.setUserId(emailId);
                enrollment.setStatus("NOT_STARTED");
                enrollment.setEnrolledTs(currentTime);
                enrollment.setEnrolledByEmailId(trainingPlan.getEmailId());
                enrollment.setEnrollmentType("TRAINING_PLAN");
                enrollment.setProgressPercent(0L);
                enrollment.setTrainingPlanId(trainingPlanId);
                enrollment.setCreatedTs(currentTime);
                enrollment.setUpdatedTs(currentTime);
                enrollment.setCreatedBy(trainingPlan.getEmailId());
                enrollment.setUpdatedBy(trainingPlan.getEmailId());
                enrollment.setCourseSummary(training);
                
                List<CourseDetail> modules = courseDetailRepository.findByCourseTrainingId(trainingId);
                for (CourseDetail module : modules) {
                    EnrollmentDetails enrollmentDetail = new EnrollmentDetails();
                    enrollmentDetail.setModuleId(module.getModuleId());
                    enrollmentDetail.setStatus("NOT_STARTED");
                    enrollmentDetail.setCreatedTs(currentTime);
                    enrollmentDetail.setUpdatedTs(currentTime);
                    enrollmentDetail.setCreatedBy(trainingPlan.getEmailId());
                    enrollmentDetail.setUpdatedBy(trainingPlan.getEmailId());
                    enrollmentDetail.setCourseDetail(module);
                    
                    enrollment.addEnrollmentDetail(enrollmentDetail);
                }
                
                enrollmentRepository.save(enrollment);
                enrollmentsCreated++;
            }
        }
        
        logger.info("Successfully created {} enrollments for training plan {}", enrollmentsCreated, trainingPlanId);
        
        return AddTraineesToPlanResponseDTO.builder()
                .trainingPlanId(trainingPlanId)
                .enrollmentsCreated(enrollmentsCreated)
                .message("Successfully added " + enrollmentsCreated + " enrollments to training plan")
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
