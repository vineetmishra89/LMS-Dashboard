package com.example.lms.repo;

import com.example.lms.domain.EmployeeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeDetailsRepository extends JpaRepository<EmployeeDetails, String> {
    
    Optional<EmployeeDetails> findByEmailIdIgnoreCase(String emailId);
    
    boolean existsByEmailIdIgnoreCase(String emailId);
}
