package com.example.lms.repo;

import com.example.lms.domain.EmployeeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeHierarchyRepository extends JpaRepository<EmployeeDetails, String> {

  @Query(value = "SELECT e.emp_id, e.email_id, e.emp_name, e.emp_designation, " +
    "e.project_name, e.ro_email_id, e.emp_active_flag " +
    "FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "WHERE e.ro_email_id = :roEmailId " +
    "AND e.emp_active_flag = 'Y'", nativeQuery = true)
  List<Object[]> findDirectReportsByRoEmailId(@Param("roEmailId") String roEmailId);

  @Query(value = "SELECT e.emp_id, e.email_id, e.emp_name, e.emp_designation, " +
    "e.project_name, e.ro_email_id, e.emp_active_flag " +
    "FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "WHERE e.email_id = :emailId", nativeQuery = true)
  Optional<Object[]> findByEmailId(@Param("emailId") String emailId);

  @Query(value = "SELECT COUNT(*) FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "WHERE e.email_id = :emailId", nativeQuery = true)
  Long countByEmailId(@Param("emailId") String emailId);

  @Query(value = "WITH RECURSIVE employee_hierarchy AS ( " +
    "SELECT e.emp_id, e.email_id, e.emp_name, e.emp_designation, " +
    "e.project_name, e.ro_email_id, e.emp_active_flag " +
    "FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "WHERE e.ro_email_id = :roEmailId " +
    "AND e.emp_active_flag = 'Y' " +
    "UNION ALL " +
    "SELECT e.emp_id, e.email_id, e.emp_name, e.emp_designation, " +
    "e.project_name, e.ro_email_id, e.emp_active_flag " +
    "FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "INNER JOIN employee_hierarchy eh ON e.ro_email_id = eh.email_id " +
    "WHERE e.emp_active_flag = 'Y' " +
    ") " +
    "SELECT emp_id, email_id, emp_name, emp_designation, " +
    "project_name, ro_email_id, emp_active_flag " +
    "FROM employee_hierarchy", nativeQuery = true)
  List<Object[]> findAllEmployeesInHierarchy(@Param("roEmailId") String roEmailId);

  @Query(value = "SELECT COUNT(*) FROM lms_schema.LMS_EMPLOYEE_DTLS e " +
    "WHERE e.ro_email_id = :roEmailId " +
    "AND e.emp_active_flag = 'Y'", nativeQuery = true)
  Long countByRoEmailIdAndActiveFlag(@Param("roEmailId") String roEmailId);
}
