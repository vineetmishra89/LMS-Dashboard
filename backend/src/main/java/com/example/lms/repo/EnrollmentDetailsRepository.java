package com.example.lms.repo;

import com.example.lms.domain.EnrollmentDetails;
import com.example.lms.domain.EnrollmentDetailsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentDetailsRepository extends JpaRepository<EnrollmentDetails, EnrollmentDetailsId> {

  EnrollmentDetails findByEnrollmentDetailsId(EnrollmentDetailsId enrollmentDetailsId);
}
