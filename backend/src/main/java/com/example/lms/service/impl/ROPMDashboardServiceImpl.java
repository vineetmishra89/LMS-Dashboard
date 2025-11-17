package com.example.lms.service.impl;

import com.example.lms.dto.EmployeeDetailsDto;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.repo.EmployeeHierarchyRepository;
import com.example.lms.repo.ProjectRepository;
import com.example.lms.service.ROPMDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ROPMDashboardServiceImpl implements ROPMDashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(ROPMDashboardServiceImpl.class);
    
    private final ProjectRepository projectRepository;
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final EmployeeHierarchyRepository employeeHierarchyRepository;
    
    public ROPMDashboardServiceImpl(ProjectRepository projectRepository,
                                    EmployeeDetailsRepository employeeDetailsRepository,
                                    EmployeeHierarchyRepository employeeHierarchyRepository) {
        this.projectRepository = projectRepository;
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.employeeHierarchyRepository = employeeHierarchyRepository;
    }
    
    @Override
    public List<String> getSbusByUser(String userId) {
        logger.info("Fetching SBUs for user: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("UserId is null or empty");
            return Collections.emptyList();
        }
        
        try {
            List<String> sbus = projectRepository.findDistinctBuByUser(userId);
            logger.info("Found {} SBUs for user: {}", sbus.size(), userId);
            return sbus;
        } catch (Exception e) {
            logger.error("Error fetching SBUs for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getProjectsByUserAndSbus(String userId, List<String> sbus) {
        logger.info("Fetching projects for user: {} with SBUs: {}", userId, sbus);
        
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("UserId is null or empty");
            return Collections.emptyList();
        }
        
        try {
            List<String> projects = projectRepository.findDistinctProjectsByUserAndSbus(userId, sbus);
            logger.info("Found {} projects for user: {}", projects.size(), userId);
            return projects;
        } catch (Exception e) {
            logger.error("Error fetching projects for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<EmployeeDetailsDto> getEmployeesForROPMDashboard(String roEmailId, List<String> projects) {
        logger.info("Fetching employees for RO/PM Dashboard. RO: {}, Projects: {}", roEmailId, projects);
        
        if (roEmailId == null || roEmailId.trim().isEmpty()) {
            logger.warn("RO email ID is null or empty");
            return Collections.emptyList();
        }
        
        try {
            Map<String, EmployeeDetailsDto> employeeMap = new LinkedHashMap<>();
            
            List<Object[]> hierarchyResults = employeeHierarchyRepository.findAllEmployeesInHierarchy(roEmailId);
            logger.debug("Found {} employees in RO hierarchy", hierarchyResults.size());
            
            for (Object[] row : hierarchyResults) {
                EmployeeDetailsDto dto = mapToEmployeeDetailsDto(row);
                employeeMap.put(dto.emailId(), dto);
            }
            
            if (projects != null && !projects.isEmpty()) {
                List<Object[]> projectEmployees = employeeDetailsRepository.findActiveEmployeesByProjectNames(projects);
                logger.debug("Found {} employees from selected projects", projectEmployees.size());
                
                for (Object[] row : projectEmployees) {
                    EmployeeDetailsDto dto = mapToEmployeeDetailsDto(row);
                    employeeMap.put(dto.emailId(), dto);
                }
            }
            
            List<EmployeeDetailsDto> result = new ArrayList<>(employeeMap.values());
            logger.info("Returning {} unique employees for RO/PM Dashboard", result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error fetching employees for RO/PM Dashboard", e);
            return Collections.emptyList();
        }
    }
    
    private EmployeeDetailsDto mapToEmployeeDetailsDto(Object[] row) {
        try {
            String empActiveFlag = row[6] instanceof Character 
                ? String.valueOf((Character) row[6]) 
                : (String) row[6];
            
            return new EmployeeDetailsDto(
                (Integer) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (String) row[5],
                empActiveFlag
            );
        } catch (Exception e) {
            logger.error("Error mapping row to EmployeeDetailsDto: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapping employee data", e);
        }
    }
}
