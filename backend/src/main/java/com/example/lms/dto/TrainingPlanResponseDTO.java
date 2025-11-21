package com.example.lms.dto;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * DTO for training plan creation response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlanResponseDTO {
    
    /**
     * Training plan ID
     */
    private Long trainingPlanId;
    
    /**
     * Training plan name
     */
    private String trainingPlanName;
    
    /**
     * User's email ID
     */
    private String emailId;
    
    /**
     * Training plan status
     */
    private String status;
    
    /**
     * Created timestamp
     */
    private OffsetDateTime createdTs;
    
    /**
     * Success message
     */
    private String message;
}
