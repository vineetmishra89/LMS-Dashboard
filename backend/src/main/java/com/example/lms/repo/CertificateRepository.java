package com.example.lms.repo;

import com.example.lms.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
  List<Certificate> findByUserId(UUID userId);
}
