package com.example.lms.repo;

import com.example.lms.domain.TrainingPlanSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingPlanSummaryRepository extends JpaRepository<TrainingPlanSummary, Long> {
    
    /**
     * Find a training plan by name and email ID (case-insensitive).
     * Used to check for duplicate training plans for the same user.
     * 
     * @param trngPlanName Training plan name
     * @param emailId User's email ID
     * @return Optional containing the training plan if found
     */
    @Query(value = "SELECT * FROM lms_schema.LMS_TRNG_PLAN_SUMMARY " +
                   "WHERE LOWER(TRNG_PLAN_NAME) = LOWER(:trngPlanName) " +
                   "  AND LOWER(EMAIL_ID) = LOWER(:emailId)", 
           nativeQuery = true)
    Optional<TrainingPlanSummary> findByTrainingPlanNameAndEmailId(
        @Param("trngPlanName") String trngPlanName, 
        @Param("emailId") String emailId
    );
    
    /**
     * Check if a training plan exists for any user (case-insensitive).
     * Used to check if a training plan name is already taken by another user.
     * 
     * @param trngPlanName Training plan name
     * @return true if the training plan exists for any user, false otherwise
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM lms_schema.LMS_TRNG_PLAN_SUMMARY " +
                   "WHERE LOWER(TRNG_PLAN_NAME) = LOWER(:trngPlanName)", 
           nativeQuery = true)
    boolean existsByTrainingPlanName(@Param("trngPlanName") String trngPlanName);
}
