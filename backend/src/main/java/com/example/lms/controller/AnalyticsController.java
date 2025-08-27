package com.example.lms.controller;

import com.example.lms.dto.AnalyticsSummaryDto;
import com.example.lms.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin
public class AnalyticsController {
  private final AnalyticsService analyticsService;
  public AnalyticsController(AnalyticsService analyticsService) { this.analyticsService = analyticsService; }

  @GetMapping("/summary")
  public AnalyticsSummaryDto summary(@RequestParam UUID userId) {
    return analyticsService.getSummary(userId);
  }
}
