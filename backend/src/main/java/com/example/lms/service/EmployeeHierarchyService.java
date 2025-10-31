package com.example.lms.service;

import com.example.lms.dto.EmployeeHierarchyResponseDto;

public interface EmployeeHierarchyService {
  
  EmployeeHierarchyResponseDto getEmployeeHierarchy(String userId);
  
  boolean isRo(String userId);
}
