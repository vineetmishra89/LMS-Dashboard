package com.example.lms.service.impl;

import com.example.lms.domain.EmployeeDetails;
import com.example.lms.domain.ProjectDetails;
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
    
    /**
     * Check if user is RO-only (has no project roles like PM/ADM/Offshore DD).
     * RO-only users should only see their own project's BU and employees in their hierarchy.
     * 
     * @param userId User's email ID
     * @return true if user is RO-only, false if user has project roles
     */
    private boolean isRoOnlyUser(String userId) {
        try {
            boolean hasProjectRole = projectRepository.hasAnyProjectRole(userId);
            logger.debug("User {} has project role: {}", userId, hasProjectRole);
            return !hasProjectRole;
        } catch (Exception e) {
            logger.error("Error checking if user {} is RO-only: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get user's own project name from LMS_EMPLOYEE_DTLS.
     * Used for RO-only users to restrict them to their own project.
     * 
     * @param userId User's email ID
     * @return User's project name, or null if not found
     */
    private String getUserProjectName(String userId) {
        try {
            Optional<EmployeeDetails> employeeOpt = employeeDetailsRepository.findByEmailIdIgnoreCase(userId);
            if (employeeOpt.isPresent()) {
                String projectName = employeeOpt.get().getProjectName();
                logger.debug("User {} project name: {}", userId, projectName);
                return projectName;
            } else {
                logger.warn("Employee not found for userId: {}", userId);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching project name for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get BU for a given project name from LMS_PROJECT_DTLS.
     * Used for RO-only users to get their project's BU.
     * 
     * @param projectName Project name
     * @return BU value, or null if not found
     */
    private String getProjectBu(String projectName) {
        try {
            Optional<ProjectDetails> projectOpt = projectRepository.findById(projectName);
            if (projectOpt.isPresent()) {
                String bu = projectOpt.get().getSbu();
                logger.debug("Project {} BU: {}", projectName, bu);
                return bu;
            } else {
                logger.warn("Project not found: {}", projectName);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error fetching BU for project {}: {}", projectName, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public List<String> getSbusByUser(String userId) {
        logger.info("Fetching SBUs for user: {}", userId);
        
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("UserId is null or empty");
            return Collections.emptyList();
        }
        
        try {
            boolean isRoOnly = isRoOnlyUser(userId);
            logger.info("User {} is RO-only: {}", userId, isRoOnly);
            
            if (isRoOnly) {
                String userProjectName = getUserProjectName(userId);
                if (userProjectName == null || userProjectName.trim().isEmpty()) {
                    logger.warn("RO-only user {} has no project name, returning empty SBU list", userId);
                    return Collections.emptyList();
                }
                
                String projectBu = getProjectBu(userProjectName);
                if (projectBu == null || projectBu.trim().isEmpty()) {
                    logger.warn("RO-only user {} project {} has no BU, returning empty SBU list", 
                               userId, userProjectName);
                    return Collections.emptyList();
                }
                
                logger.info("RO-only user {} - returning single SBU: {}", userId, projectBu);
                return Collections.singletonList(projectBu);
            } else {
                List<String> sbus = projectRepository.findDistinctBuByUser(userId);
                logger.info("User with project roles {} - found {} SBUs", userId, sbus.size());
                return sbus;
            }
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
            boolean isRoOnly = isRoOnlyUser(userId);
            logger.info("User {} is RO-only: {}", userId, isRoOnly);
            
            if (isRoOnly) {
                String userProjectName = getUserProjectName(userId);
                if (userProjectName == null || userProjectName.trim().isEmpty()) {
                    logger.warn("RO-only user {} has no project name, returning empty project list", userId);
                    return Collections.emptyList();
                }
                
                logger.info("RO-only user {} - returning single project: {}", userId, userProjectName);
                return Collections.singletonList(userProjectName);
            } else {
                List<String> projects = projectRepository.findDistinctProjectsByUserAndSbus(userId, sbus);
                logger.info("User with project roles {} - found {} projects", userId, projects.size());
                return projects;
            }
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
            boolean isRoOnly = isRoOnlyUser(roEmailId);
            logger.info("User {} is RO-only: {}", roEmailId, isRoOnly);
            
            Map<String, EmployeeDetailsDto> employeeMap = new LinkedHashMap<>();
            
            List<Object[]> hierarchyResults = employeeHierarchyRepository.findAllEmployeesInHierarchy(roEmailId);
            logger.debug("Found {} employees in RO hierarchy", hierarchyResults.size());
            
            for (Object[] row : hierarchyResults) {
                EmployeeDetailsDto dto = mapToEmployeeDetailsDto(row);
                employeeMap.put(dto.emailId(), dto);
            }
            
            if (!isRoOnly && projects != null && !projects.isEmpty()) {
                logger.info("User has project roles - adding employees from selected projects");
                List<Object[]> projectEmployees = employeeDetailsRepository.findActiveEmployeesByProjectNames(projects);
                logger.debug("Found {} employees from selected projects", projectEmployees.size());
                
                for (Object[] row : projectEmployees) {
                    EmployeeDetailsDto dto = mapToEmployeeDetailsDto(row);
                    employeeMap.put(dto.emailId(), dto);
                }
            } else if (isRoOnly) {
                logger.info("RO-only user - ignoring project filters, returning only RO hierarchy employees");
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
