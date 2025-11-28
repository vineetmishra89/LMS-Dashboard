package com.example.lms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTraineesToPlanRequestDTO {
    
    private Long trainingPlanId;
    
    private List<Long> trainingIds;
    
    private List<String> emailIds;
}
