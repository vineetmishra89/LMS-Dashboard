package com.example.lms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTraineesToPlanResponseDTO {
    
    private Long trainingPlanId;
    
    private Integer enrollmentsCreated;
    
    private String message;
}
