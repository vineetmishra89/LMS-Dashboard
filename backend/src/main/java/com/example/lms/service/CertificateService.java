package com.example.lms.service;

import com.example.lms.domain.Certificate;
import com.example.lms.repo.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {
  private final CertificateRepository certificateRepository;
  public CertificateService(CertificateRepository certificateRepository) { this.certificateRepository = certificateRepository; }

  public List<Certificate> byUser(UUID userId) {
    return certificateRepository.findByUserId(userId);
  }
}
