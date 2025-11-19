package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * DTO for user roles response.
 * Contains a list of role strings that the user possesses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesDTO {
    
    /**
     * List of role strings (e.g., "ROLE_LND_ADMIN", "ROLE_RO", "ROLE_PROJECT_MANAGER")
     */
    private List<String> roles;
    
    /**
     * Convenience method to check if user has a specific role.
     * 
     * @param role Role constant to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Convenience method to check if user has any of the specified roles.
     * 
     * @param roles Role constants to check
     * @return true if user has at least one of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(this.roles::contains);
    }
    
    /**
     * Convenience method to check if user has all of the specified roles.
     * 
     * @param roles Role constants to check
     * @return true if user has all of the roles, false otherwise
     */
    public boolean hasAllRoles(String... roles) {
        if (this.roles == null) {
            return false;
        }
        return Arrays.stream(roles).allMatch(this.roles::contains);
    }
}
