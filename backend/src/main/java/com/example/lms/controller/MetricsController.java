package com.example.lms.controller;

import com.example.lms.dto.MetricsRequestDto;
import com.example.lms.dto.MetricsResponseDto;
import com.example.lms.service.MetricsExcelExportService;
import com.example.lms.service.MetricsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin
public class MetricsController {

  private final MetricsService metricsService;
  private final MetricsExcelExportService excelExportService;

  public MetricsController(MetricsService metricsService, MetricsExcelExportService excelExportService) {
    this.metricsService = metricsService;
    this.excelExportService = excelExportService;
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
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required = false, defaultValue = "10") Integer topN) {
    
    MetricsRequestDto request = new MetricsRequestDto();
    request.setCategory(category);
    request.setLevel(level);
    request.setTechnology(technology);
    request.setStartDate(startDate);
    request.setEndDate(endDate);
    request.setTopN(topN);
    
    return metricsService.getMetrics(request);
  }

  @GetMapping("/report/download")
  public ResponseEntity<byte[]> downloadMetricsReport(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String level,
    @RequestParam(required = false) String technology,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required = false, defaultValue = "10") Integer topN) throws IOException {
    
    MetricsRequestDto request = new MetricsRequestDto();
    request.setCategory(category);
    request.setLevel(level);
    request.setTechnology(technology);
    request.setStartDate(startDate);
    request.setEndDate(endDate);
    request.setTopN(topN);
    
    MetricsResponseDto metricsData = metricsService.getMetrics(request);
    byte[] excelBytes = excelExportService.generateMetricsExcel(metricsData);
    
    String filename = generateFilename(startDate, endDate);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(excelBytes.length);
    
    return ResponseEntity.ok()
      .headers(headers)
      .body(excelBytes);
  }

  private String generateFilename(LocalDate startDate, LocalDate endDate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    StringBuilder filename = new StringBuilder("metrics_report");
    
    if (startDate != null) {
      filename.append("_from_").append(startDate.format(formatter));
    }
    if (endDate != null) {
      filename.append("_to_").append(endDate.format(formatter));
    }
    
    filename.append(".xlsx");
    return filename.toString();
  }
}
