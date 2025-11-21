package com.example.lms.dto;

import lombok.*;

/**
 * DTO for training plan creation request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlanRequestDTO {
    
    /**
     * Training plan name
     */
    private String trainingPlanName;
    
    /**
     * User's email ID
     */
    private String emailId;
}
