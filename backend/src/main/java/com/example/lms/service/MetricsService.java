package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.repo.MetricsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricsService {

  private final MetricsRepository metricsRepository;

  public MetricsService(MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
  }

  public MetricsResponseDto getMetrics(MetricsRequestDto request) {
    OffsetDateTime startDate = calculateStartDate(request.getTimePeriod());
    Integer topN = request.getTopN() != null ? request.getTopN() : 10;

    MetricsResponseDto response = new MetricsResponseDto();
    
    response.setTopTrainees(getTopTrainees(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopRatedCourses(getTopRatedCourses(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopEnrolledCourses(getTopEnrolledCourses(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopViewedCourses(getTopViewedCourses(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setTopCompletedCourses(getTopCompletedCourses(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology(), topN));
    
    response.setUserTrainingDump(getUserTrainingDump(startDate, request.getCategory(), 
      request.getLevel(), request.getTechnology()));

    return response;
  }

  private OffsetDateTime calculateStartDate(TimePeriod timePeriod) {
    if (timePeriod == null) {
      timePeriod = TimePeriod.LAST_WEEK;
    }
    return OffsetDateTime.now().minusDays(timePeriod.getDays());
  }

  private List<TopTraineeDto> getTopTrainees(OffsetDateTime startDate, String category, 
                                              String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopTrainees(startDate, category, level, technology, topN);
    return results.stream().map(row -> new TopTraineeDto(
      (String) row[0],
      ((Number) row[1]).longValue(),
      row[2] != null ? ((BigDecimal) row[2]).doubleValue() : 0.0,
      ((Number) row[3]).longValue()
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopRatedCourses(OffsetDateTime startDate, String category, 
                                                 String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopRatedCourses(startDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Number) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (String) row[4],
      ((Number) row[5]).longValue(),
      0L,
      0L
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopEnrolledCourses(OffsetDateTime startDate, String category, 
                                                    String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopEnrolledCourses(startDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Number) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (String) row[4],
      ((Number) row[5]).longValue(),
      0L,
      0L
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopViewedCourses(OffsetDateTime startDate, String category, 
                                                  String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopViewedCourses(startDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Number) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (String) row[4],
      ((Number) row[5]).longValue(),
      0L,
      ((Number) row[7]).longValue()
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopCompletedCourses(OffsetDateTime startDate, String category, 
                                                     String level, String technology, Integer topN) {
    List<Object[]> results = metricsRepository.findTopCompletedCourses(startDate, category, level, technology, topN);
    return results.stream().map(row -> new TopCourseDto(
      ((Number) row[0]).longValue(),
      (String) row[1],
      (String) row[2],
      (String) row[3],
      (String) row[4],
      ((Number) row[5]).longValue(),
      ((Number) row[6]).longValue(),
      0L
    )).collect(Collectors.toList());
  }

  private List<UserTrainingDumpDto> getUserTrainingDump(OffsetDateTime startDate, String category, 
                                                         String level, String technology) {
    List<Object[]> results = metricsRepository.findUserTrainingDump(startDate, category, level, technology);
    return results.stream().map(row -> new UserTrainingDumpDto(
      (String) row[0],
      ((Number) row[1]).longValue(),
      (String) row[2],
      (String) row[3],
      (String) row[4],
      (String) row[5],
      row[6] != null ? ((Number) row[6]).longValue() : 0L,
      row[7] != null ? ((Timestamp) row[7]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null,
      row[8] != null ? ((Timestamp) row[8]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null,
      row[9] != null ? ((Timestamp) row[9]).toInstant().atOffset(OffsetDateTime.now().getOffset()) : null
    )).collect(Collectors.toList());
  }
}
