package com.example.lms.service.impl;

import com.example.lms.constants.RoleConstants;
import com.example.lms.dto.UserRolesDTO;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.repo.ProjectRepository;
import com.example.lms.repo.UserRoleRepository;
import com.example.lms.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserRoleService.
 * Aggregates roles from multiple sources (database roles, employee hierarchy, project roles).
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRoleServiceImpl.class);
    
    private final UserRoleRepository userRoleRepository;
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final ProjectRepository projectRepository;
    
    public UserRoleServiceImpl(UserRoleRepository userRoleRepository,
                              EmployeeDetailsRepository employeeDetailsRepository,
                              ProjectRepository projectRepository) {
        this.userRoleRepository = userRoleRepository;
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.projectRepository = projectRepository;
    }
    
    @Override
    public UserRolesDTO getUserRoles(String emailId) {
        logger.info("Fetching roles for user: {}", emailId);
        
        List<String> roles = new ArrayList<>();
        
        try {
            boolean isEmployee = employeeDetailsRepository.existsByEmailIdIgnoreCase(emailId);
            if (isEmployee) {
                roles.add(RoleConstants.ROLE_TRAINEE);
                logger.debug("User {} is an employee, added ROLE_TRAINEE", emailId);
            } else {
                logger.warn("User {} not found in LMS_EMPLOYEE_DTLS, no ROLE_TRAINEE assigned", emailId);
            }
        } catch (Exception e) {
            logger.error("Error checking employee status for user {}: {}", emailId, e.getMessage(), e);
        }
        
        roles.addAll(getDatabaseRoles(emailId));
        
        if (isReportingOfficer(emailId)) {
            roles.add(RoleConstants.ROLE_RO);
            logger.debug("User {} is a Reporting Officer", emailId);
        }
        
        roles.addAll(getProjectRoles(emailId));
        
        roles = roles.stream().distinct().collect(Collectors.toList());
        
        logger.info("User {} has {} roles: {}", emailId, roles.size(), roles);
        
        return UserRolesDTO.builder()
                .roles(roles)
                .build();
    }
    
    @Override
    public boolean hasRole(String emailId, String role) {
        UserRolesDTO userRoles = getUserRoles(emailId);
        return userRoles.hasRole(role);
    }
    
    /**
     * Get database roles from LMS_USER_ROLES + LMS_ROLE_DETAILS.
     * Maps database role names to application role constants.
     * 
     * @param emailId User's email ID
     * @return List of application role constants
     */
    private List<String> getDatabaseRoles(String emailId) {
        List<String> roles = new ArrayList<>();
        
        try {
            List<String> roleNames = userRoleRepository.findRoleNamesByEmail(emailId);
            
            for (String roleName : roleNames) {
                String mappedRole = mapDatabaseRoleToAppRole(roleName);
                if (mappedRole != null) {
                    roles.add(mappedRole);
                    logger.debug("Mapped database role '{}' to '{}'", roleName, mappedRole);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching database roles for user {}: {}", emailId, e.getMessage(), e);
        }
        
        return roles;
    }
    
    /**
     * Map database role name to application role constant.
     * 
     * @param roleName Database role name (e.g., "lnd", "admin", "trainer")
     * @return Application role constant or null if not mapped
     */
    private String mapDatabaseRoleToAppRole(String roleName) {
        if (roleName == null) {
            return null;
        }
        
        String lowerRoleName = roleName.toLowerCase().trim();
        
        switch (lowerRoleName) {
            case "lnd":
                return RoleConstants.ROLE_LND_ADMIN;
            case "trainer":
                return RoleConstants.ROLE_TRAINER;
            default:
                logger.debug("No mapping found for database role: {}", roleName);
                return null;
        }
    }
    
    /**
     * Check if user is a Reporting Officer (has employees reporting to them).
     * 
     * @param emailId User's email ID
     * @return true if user is an RO, false otherwise
     */
    private boolean isReportingOfficer(String emailId) {
        try {
            return employeeDetailsRepository.isReportingOfficer(emailId);
        } catch (Exception e) {
            logger.error("Error checking RO status for user {}: {}", emailId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get project roles from LMS_PROJECT_DTLS.
     * 
     * @param emailId User's email ID
     * @return List of project role constants
     */
    private List<String> getProjectRoles(String emailId) {
        List<String> roles = new ArrayList<>();
        
        try {
            List<Object[]> projectRoles = projectRepository.findProjectRolesByEmail(emailId);
            
            if (projectRoles != null && !projectRoles.isEmpty()) {
                Object[] roleFlags = projectRoles.get(0);
                
                
                if (isRoleFlagSet(roleFlags[0])) {
                    roles.add(RoleConstants.ROLE_PROJECT_MANAGER);
                    logger.debug("User {} is a Project Manager", emailId);
                }
                
                if (isRoleFlagSet(roleFlags[1])) {
                    roles.add(RoleConstants.ROLE_PROJECT_ADMIN);
                    logger.debug("User {} is a Project Admin", emailId);
                }
                
                if (isRoleFlagSet(roleFlags[2])) {
                    roles.add(RoleConstants.ROLE_OFFSHORE_DD);
                    logger.debug("User {} is an Offshore DD", emailId);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching project roles for user {}: {}", emailId, e.getMessage(), e);
        }
        
        return roles;
    }
    
    /**
     * Check if a role flag from database query is set (equals 1).
     * Handles both BigInteger and Integer types.
     * 
     * @param flag Role flag from database query
     * @return true if flag is 1, false otherwise
     */
    private boolean isRoleFlagSet(Object flag) {
        if (flag == null) {
            return false;
        }
        
        if (flag instanceof BigInteger) {
            return ((BigInteger) flag).intValue() == 1;
        } else if (flag instanceof Integer) {
            return ((Integer) flag) == 1;
        } else if (flag instanceof Long) {
            return ((Long) flag) == 1;
        } else if (flag instanceof Number) {
            return ((Number) flag).intValue() == 1;
        }
        
        return false;
    }
}
