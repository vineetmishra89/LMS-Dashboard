package com.example.lms.service;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.EmployeeHierarchyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeHierarchyService {

  private static final Logger logger = LoggerFactory.getLogger(EmployeeHierarchyService.class);

  private final EmployeeHierarchyRepository employeeHierarchyRepository;

  public EmployeeHierarchyService(EmployeeHierarchyRepository employeeHierarchyRepository) {
    this.employeeHierarchyRepository = employeeHierarchyRepository;
  }

  public EmployeeHierarchyResponseDto getEmployeeHierarchy(String userId) {
    logger.info("Fetching employee hierarchy for userId: {}", userId);

    if (userId == null || userId.trim().isEmpty()) {
      logger.error("UserId is null or empty");
      throw new ValidationException("UserId cannot be null or empty");
    }

    Long employeeCount = employeeHierarchyRepository.countByEmailId(userId);
    if (employeeCount == 0) {
      logger.error("Employee not found with emailId: {}", userId);
      throw new ResourceNotFoundException("Employee not found with emailId: " + userId);
    }

    logger.debug("Employee found with emailId: {}", userId);

    List<EmployeeDetailsDto> allEmployees = new ArrayList<>();
    Set<String> visitedEmployees = new HashSet<>();

    try {
      fetchAllReportsRecursively(userId, allEmployees, visitedEmployees);
      
      logger.info("Successfully fetched {} employees in hierarchy for userId: {}", allEmployees.size(), userId);
      
      return new EmployeeHierarchyResponseDto(
        userId,
        allEmployees.size(),
        allEmployees
      );
    } catch (Exception e) {
      logger.error("Error fetching employee hierarchy for userId: {}", userId, e);
      throw e;
    }
  }

  private void fetchAllReportsRecursively(String roEmailId, List<EmployeeDetailsDto> allEmployees, 
                                          Set<String> visitedEmployees) {
    if (visitedEmployees.contains(roEmailId)) {
      logger.warn("Circular reference detected for emailId: {}", roEmailId);
      return;
    }

    visitedEmployees.add(roEmailId);

    try {
      List<Object[]> directReports = employeeHierarchyRepository.findDirectReportsByRoEmailId(roEmailId);
      
      logger.debug("Found {} direct reports for roEmailId: {}", directReports.size(), roEmailId);

      for (Object[] row : directReports) {
        EmployeeDetailsDto employee = mapToEmployeeDetailsDto(row);
        allEmployees.add(employee);

        fetchAllReportsRecursively(employee.emailId(), allEmployees, visitedEmployees);
      }
    } catch (Exception e) {
      logger.error("Error fetching direct reports for roEmailId: {}", roEmailId, e);
      throw new RuntimeException("Error fetching employee hierarchy data", e);
    }
  }

  private EmployeeDetailsDto mapToEmployeeDetailsDto(Object[] row) {
    try {
      return new EmployeeDetailsDto(
        (Integer) row[0],
        (String) row[1],
        (String) row[2],
        (String) row[3],
        (String) row[4],
        (String) row[5],
        (String) row[6]
      );
    } catch (Exception e) {
      logger.error("Error mapping row to EmployeeDetailsDto: {}", e.getMessage(), e);
      throw new RuntimeException("Error mapping employee data", e);
    }
  }
}
