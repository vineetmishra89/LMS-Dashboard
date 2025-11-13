package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.repo.MetricsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricsService {

  private final MetricsRepository metricsRepository;

  public MetricsService(MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
  }

  public MetricsResponseDto getMetrics(MetricsRequestDto request) {
    OffsetDateTime startDate = convertToOffsetDateTime(request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusMonths(6));
    OffsetDateTime endDate = convertToOffsetDateTime(request.getEndDate() != null ?request.getEndDate() : LocalDate.now());
    Integer topN = request.getTopN() != null ? request.getTopN() : 10;

    MetricsResponseDto response = new MetricsResponseDto();
    
    response.setTopTrainees(getTopTrainees(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopRatedCourses(getTopRatedCourses(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopEnrolledCourses(getTopEnrolledCourses(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopViewedCourses(getTopViewedCourses(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopCompletedCourses(getTopCompletedCourses(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setUserTrainingDump(getUserTrainingDump(startDate, endDate, request.getCategory(), 
      request.getLevel(), request.getTechnology()));

    return response;
  }

  private OffsetDateTime convertToOffsetDateTime(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
  }

  private List<TopTraineeDto> getTopTrainees(OffsetDateTime startDate, OffsetDateTime endDate, 
                                              String category, String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopTrainees(startDate, endDate, category, level, technology, topN);
    return results.stream().map(row -> new TopTraineeDto(
      (String) row[0],
      ((Long) row[1]).longValue(),
      row[2] != null ? ((BigDecimal) row[2]).doubleValue() : 0.0,
      ((Long) row[3]).longValue()
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopRatedCourses(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                 String category, String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopRatedCourses(startDate, endDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Long) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (BigDecimal) row[4],
      ((Long) row[5]).longValue(),
      0L,
      0L
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopEnrolledCourses(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                    String category, String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopEnrolledCourses(startDate, endDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Long) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (BigDecimal) row[4],
      ((Long) row[5]).longValue(),
      0L,
      0L
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopViewedCourses(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                  String category, String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopViewedCourses(startDate, endDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Long) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (BigDecimal) row[4],
      ((Long) row[5]).longValue(),
      0L,
      ((Long) row[7]).longValue()
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopCompletedCourses(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                     String category, String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopCompletedCourses(startDate, endDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Long) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (BigDecimal) row[4],
      ((Long) row[5]).longValue(),
      ((Long) row[6]).longValue(),
      0L
    )).collect(Collectors.toList());
  }

  private List<UserTrainingDumpDto> getUserTrainingDump(OffsetDateTime startDate, OffsetDateTime endDate, 
                                                         String category, String level, String technology) {
    List<Object[]> results = metricsRepository.findUserTrainingDump(startDate, endDate, category, level, technology);
    return results.stream().map(row -> new UserTrainingDumpDto(
      (String) row[0],
      ((Long) row[1]).longValue(),
      (String) row[2],
      (String) row[3],
      (String) row[4],
      (String) row[5],
      row[6] != null ? ((Long) row[6]).longValue() : 0L,
      row[7] != null ? ((Timestamp) row[7]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null,
      row[8] != null ? ((Timestamp) row[8]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null,
      row[9] != null ? ((Timestamp) row[9]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null
    )).collect(Collectors.toList());
  }
}
