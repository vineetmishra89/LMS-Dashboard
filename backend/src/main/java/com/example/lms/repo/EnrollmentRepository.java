package com.example.lms.repo;

import com.example.lms.domain.EnrollmentMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentMapping, Long> {
  List<EnrollmentMapping> findByUserId(String userId);
}
