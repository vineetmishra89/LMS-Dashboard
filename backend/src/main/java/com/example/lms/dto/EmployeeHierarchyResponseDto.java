package com.example.lms.dto;

import java.util.List;

public record EmployeeHierarchyResponseDto(
  String roEmailId,
  Integer totalEmployees,
  List<EmployeeDetailsDto> employees
) {}
