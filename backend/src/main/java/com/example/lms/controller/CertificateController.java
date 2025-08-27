package com.example.lms.controller;

import com.example.lms.domain.Certificate;
import com.example.lms.service.CertificateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin
public class CertificateController {
  private final CertificateService certificateService;
  public CertificateController(CertificateService certificateService) { this.certificateService = certificateService; }

  @GetMapping
  public List<Certificate> byUser(@RequestParam UUID userId) {
    return certificateService.byUser(userId);
  }
}
