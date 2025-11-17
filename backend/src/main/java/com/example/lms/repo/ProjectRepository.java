package com.example.lms.repo;

import com.example.lms.domain.ProjectDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ProjectDetails entity.
 * Provides methods to query project roles from LMS_PROJECT_DTLS table.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectDetails, String> {
    
    /**
     * Find all project roles for a given user email.
     * Returns a list of Object arrays where:
     * - index 0: is_pm (1 if user is PM, 0 otherwise)
     * - index 1: is_adm (1 if user is ADM, 0 otherwise)
     * - index 2: is_offshore_dd (1 if user is Offshore DD, 0 otherwise)
     * 
     * @param emailId User's email ID
     * @return List of Object[] with role flags
     */
    @Query(value = "SELECT " +
                   "  MAX(CASE WHEN LOWER(pm_email_id) = LOWER(:emailId) THEN 1 ELSE 0 END) as is_pm, " +
                   "  MAX(CASE WHEN LOWER(adm_email_id) = LOWER(:emailId) THEN 1 ELSE 0 END) as is_adm, " +
                   "  MAX(CASE WHEN LOWER(offshore_dd_email_id) = LOWER(:emailId) THEN 1 ELSE 0 END) as is_offshore_dd " +
                   "FROM lms_schema.LMS_PROJECT_DTLS " +
                   "WHERE (LOWER(pm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(adm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(offshore_dd_email_id) = LOWER(:emailId)) " +
                   "  AND proj_active_flag = 'Y'", 
           nativeQuery = true)
    List<Object[]> findProjectRolesByEmail(@Param("emailId") String emailId);
    
    /**
     * Check if user has any project role (PM, ADM, or Offshore DD).
     * 
     * @param emailId User's email ID
     * @return true if user has any project role, false otherwise
     */
    @Query(value = "SELECT COUNT(*) > 0 " +
                   "FROM lms_schema.LMS_PROJECT_DTLS " +
                   "WHERE (LOWER(pm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(adm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(offshore_dd_email_id) = LOWER(:emailId)) " +
                   "  AND proj_active_flag = 'Y'", 
           nativeQuery = true)
    boolean hasAnyProjectRole(@Param("emailId") String emailId);
    
    /**
     * Find distinct BU values for projects where user has any project role.
     * Used for RO/PM Dashboard SBU filter.
     * 
     * @param emailId User's email ID
     * @return List of distinct BU values
     */
    @Query(value = "SELECT DISTINCT bu " +
                   "FROM lms_schema.LMS_PROJECT_DTLS " +
                   "WHERE proj_active_flag = 'Y' " +
                   "  AND bu IS NOT NULL " +
                   "  AND (LOWER(pm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(adm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(offshore_dd_email_id) = LOWER(:emailId)) " +
                   "ORDER BY bu", 
           nativeQuery = true)
    List<String> findDistinctBuByUser(@Param("emailId") String emailId);
    
    /**
     * Find distinct project names for projects where user has any project role.
     * Optionally filtered by BU values.
     * Used for RO/PM Dashboard Project filter.
     * 
     * @param emailId User's email ID
     * @param sbus List of BU values to filter by (optional)
     * @return List of distinct project names
     */
    @Query(value = "SELECT DISTINCT project_name " +
                   "FROM lms_schema.LMS_PROJECT_DTLS " +
                   "WHERE proj_active_flag = 'Y' " +
                   "  AND (LOWER(pm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(adm_email_id) = LOWER(:emailId) " +
                   "   OR LOWER(offshore_dd_email_id) = LOWER(:emailId)) " +
                   "  AND (:sbus IS NULL OR bu IN (:sbus)) " +
                   "ORDER BY project_name", 
           nativeQuery = true)
    List<String> findDistinctProjectsByUserAndSbus(@Param("emailId") String emailId, 
                                                     @Param("sbus") List<String> sbus);
}
