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
}
