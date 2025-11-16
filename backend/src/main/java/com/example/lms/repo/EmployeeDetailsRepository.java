package com.example.lms.repo;

import com.example.lms.domain.EmployeeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDetailsRepository extends JpaRepository<EmployeeDetails, String> {
    
    Optional<EmployeeDetails> findByEmailIdIgnoreCase(String emailId);
    
    boolean existsByEmailIdIgnoreCase(String emailId);
    
    List<EmployeeDetails> findByEmpNameIgnoreCase(String empName);
    
    /**
     * Count employees who have the given email as their RO (Reporting Officer).
     * Used to determine if a user is an RO (has employees reporting to them).
     * 
     * @param roEmailId Email ID to check as RO
     * @return Count of active employees reporting to this RO
     */
    @Query(value = "SELECT COUNT(*) " +
                   "FROM lms_schema.LMS_EMPLOYEE_DTLS " +
                   "WHERE LOWER(ro_email_id) = LOWER(:roEmailId) " +
                   "  AND emp_active_flag = 'Y'", 
           nativeQuery = true)
    Long countByRoEmailIdAndActiveFlag(@Param("roEmailId") String roEmailId);
    
    /**
     * Check if user is a Reporting Officer (has active employees reporting to them).
     * 
     * @param roEmailId Email ID to check as RO
     * @return true if user is an RO, false otherwise
     */
    @Query(value = "SELECT COUNT(*) > 0 " +
                   "FROM lms_schema.LMS_EMPLOYEE_DTLS " +
                   "WHERE LOWER(ro_email_id) = LOWER(:roEmailId) " +
                   "  AND emp_active_flag = 'Y'", 
           nativeQuery = true)
    boolean isReportingOfficer(@Param("roEmailId") String roEmailId);
}
