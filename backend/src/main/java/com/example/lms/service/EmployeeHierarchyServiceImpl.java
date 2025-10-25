package com.example.lms.service;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.dto.EmployeeHierarchyResponseDto;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.exception.ValidationException;
import com.example.lms.repo.EmployeeHierarchyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeHierarchyServiceImpl implements EmployeeHierarchyService {

  private static final Logger logger = LoggerFactory.getLogger(EmployeeHierarchyServiceImpl.class);

  private final EmployeeHierarchyRepository employeeHierarchyRepository;

  public EmployeeHierarchyServiceImpl(EmployeeHierarchyRepository employeeHierarchyRepository) {
    this.employeeHierarchyRepository = employeeHierarchyRepository;
  }

  @Override
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

    try {
      List<Object[]> hierarchyResults = employeeHierarchyRepository.findAllEmployeesInHierarchy(userId);
      
      logger.debug("Found {} employees in hierarchy using CONNECT BY PRIOR for userId: {}", 
        hierarchyResults.size(), userId);

      List<EmployeeDetailsDto> allEmployees = hierarchyResults.stream()
        .map(this::mapToEmployeeDetailsDto)
        .collect(Collectors.toList());
      
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
