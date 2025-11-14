package com.example.lms.repo;

import com.example.lms.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UserRole entity.
 * Provides methods to query user roles from LMS_USER_ROLES table.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    
    /**
     * Find all role names for a given user email.
     * Joins LMS_USER_ROLES with LMS_ROLE_DETAILS to get role names.
     * 
     * @param emailId User's email ID
     * @return List of role names (e.g., "lnd", "admin", "trainer")
     */
    @Query(value = "SELECT LOWER(rd.role_name) " +
                   "FROM lms_schema.LMS_USER_ROLES ur " +
                   "JOIN lms_schema.LMS_ROLE_DETAILS rd ON ur.role_id = rd.role_id " +
                   "WHERE LOWER(ur.email_id) = LOWER(:emailId)", 
           nativeQuery = true)
    List<String> findRoleNamesByEmail(@Param("emailId") String emailId);
    
    /**
     * Check if user has a specific role by role name.
     * 
     * @param emailId User's email ID
     * @param roleName Role name to check (e.g., "lnd")
     * @return true if user has the role, false otherwise
     */
    @Query(value = "SELECT COUNT(*) > 0 " +
                   "FROM lms_schema.LMS_USER_ROLES ur " +
                   "JOIN lms_schema.LMS_ROLE_DETAILS rd ON ur.role_id = rd.role_id " +
                   "WHERE LOWER(ur.email_id) = LOWER(:emailId) " +
                   "AND LOWER(rd.role_name) = LOWER(:roleName)", 
           nativeQuery = true)
    boolean hasRole(@Param("emailId") String emailId, @Param("roleName") String roleName);
}
