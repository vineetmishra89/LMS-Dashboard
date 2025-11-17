package com.example.lms.service;

import com.example.lms.dto.EmployeeDetailsDto;

import java.util.List;

public interface ROPMDashboardService {
    
    /**
     * Get distinct SBU values for projects where user has any project role.
     * 
     * @param userId User's email ID
     * @return List of distinct SBU values
     */
    List<String> getSbusByUser(String userId);
    
    /**
     * Get distinct project names for projects where user has any project role.
     * Optionally filtered by SBU values.
     * 
     * @param userId User's email ID
     * @param sbus List of SBU values to filter by (optional)
     * @return List of distinct project names
     */
    List<String> getProjectsByUserAndSbus(String userId, List<String> sbus);
    
    /**
     * Get employees for RO/PM Dashboard.
     * Returns union of:
     * - Employees from selected projects (if projects specified)
     * - Employees in RO hierarchy (always included)
     * 
     * @param roEmailId RO's email ID
     * @param projects List of project names to filter by (optional)
     * @return List of employee details (de-duplicated by email)
     */
    List<EmployeeDetailsDto> getEmployeesForROPMDashboard(String roEmailId, List<String> projects);
}
