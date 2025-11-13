package com.example.lms.service;

import com.example.lms.dto.UserRolesDTO;

/**
 * Service interface for user role operations.
 * Provides methods to retrieve and check user roles.
 */
public interface UserRoleService {
    
    /**
     * Get all roles for a user by email.
     * Returns a DTO containing a list of role strings.
     * 
     * Roles are determined from:
     * - LMS_USER_ROLES + LMS_ROLE_DETAILS (database roles like L&D Admin)
     * - LMS_EMPLOYEE_DTLS (RO role if user has employees reporting to them)
     * - LMS_PROJECT_DTLS (project roles like PM, ADM, Offshore DD)
     * 
     * @param emailId User's email ID
     * @return UserRolesDTO containing list of role strings
     */
    UserRolesDTO getUserRoles(String emailId);
    
    /**
     * Check if user has a specific role.
     * 
     * @param emailId User's email ID
     * @param role Role constant to check (e.g., RoleConstants.ROLE_LND_ADMIN)
     * @return true if user has the role, false otherwise
     */
    boolean hasRole(String emailId, String role);
}
