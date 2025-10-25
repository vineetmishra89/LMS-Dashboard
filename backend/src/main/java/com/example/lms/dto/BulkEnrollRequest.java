package com.example.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkEnrollRequest {
  @NotBlank(message = "User ID cannot be blank")
  private String userId;
  
  @NotEmpty(message = "Email list cannot be empty")
  private List<String> emailIdList;
  
  @NotEmpty(message = "Course list cannot be empty")
  private List<Long> courseIdList;
  
  private String enrollmentType;
}
