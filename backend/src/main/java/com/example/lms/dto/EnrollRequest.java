package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollRequest {
  @NotBlank(message = "User ID cannot be blank")
  private String userId;
  
  @NotNull(message = "Course ID cannot be null")
  private Long courseId;
  
  private String enrollmentType;
}
