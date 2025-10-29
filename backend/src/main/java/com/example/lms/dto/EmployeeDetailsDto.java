package com.example.lms.dto;

public record EmployeeDetailsDto(
  Integer empId,
  String emailId,
  String empName,
  String empDesignation,
  String projectName,
  String roEmailId,
  String empActiveFlag
) {}
