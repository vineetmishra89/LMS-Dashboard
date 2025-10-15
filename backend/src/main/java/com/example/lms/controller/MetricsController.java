package com.example.lms.controller;

import com.example.lms.dto.MetricsRequestDto;
import com.example.lms.dto.MetricsResponseDto;
import com.example.lms.dto.TimePeriod;
import com.example.lms.service.MetricsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin
public class MetricsController {

  private final MetricsService metricsService;

  public MetricsController(MetricsService metricsService) {
    this.metricsService = metricsService;
  }

  @PostMapping("/report")
  public MetricsResponseDto getMetricsReport(@RequestBody MetricsRequestDto request) {
    return metricsService.getMetrics(request);
  }

  @GetMapping("/report")
  public MetricsResponseDto getMetricsReportByParams(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String level,
    @RequestParam(required = false) String technology,
    @RequestParam(required = false) TimePeriod timePeriod,
    @RequestParam(required = false, defaultValue = "10") Integer topN) {
    
    MetricsRequestDto request = new MetricsRequestDto();
    request.setCategory(category);
    request.setLevel(level);
    request.setTechnology(technology);
    request.setTimePeriod(timePeriod);
    request.setTopN(topN);
    
    return metricsService.getMetrics(request);
  }
}
