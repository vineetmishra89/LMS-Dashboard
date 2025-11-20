package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.repo.MetricsRepository;
import com.example.lms.service.impl.ROPMDashboardServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricsService {

  private final MetricsRepository metricsRepository;
  private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
  public MetricsService(MetricsRepository metricsRepository) {
    this.metricsRepository = metricsRepository;
  }

  public MetricsResponseDto getMetrics(MetricsRequestDto request) {
    logger.debug("inside getMetrics");
    return getMetricsForEmployees(request, null);
  }

  public MetricsResponseDto getMetricsForEmployees(MetricsRequestDto request, List<String> employeeEmails) {
    logger.debug("inside getMetricsForEmployees with emailList {}",employeeEmails);
    OffsetDateTime startDate = convertToOffsetDateTime(request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(60));
    OffsetDateTime endDate = convertToOffsetDateTime(request.getEndDate() != null ?request.getEndDate() : LocalDate.now());
    Integer topN = request.getTopN() != null ? request.getTopN() : 10;

    logger.debug("going inside getTopTrainees:: {}",employeeEmails);
    MetricsResponseDto response = new MetricsResponseDto();
    logger.debug("initialized MetricsResponseDto:: {}",employeeEmails);

    response.setTopTrainees(getTopTrainees(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), topN, employeeEmails));
    logger.debug("got top trainees {}",response.getTopTrainees().size());

    response.setTopRatedCourses(getTopRatedCourses(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), topN, employeeEmails));
    logger.debug("got top rated courses {}",response.getTopRatedCourses().size());

    response.setTopEnrolledCourses(getTopEnrolledCourses(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), topN, employeeEmails));

    response.setTopViewedCourses(getTopViewedCourses(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), topN, employeeEmails));

    response.setTopCompletedCourses(getTopCompletedCourses(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), topN, employeeEmails));

    response.setUserTrainingDump(getUserTrainingDump(startDate, endDate, request.getCategory(),
      request.getLevel(), request.getTechnology(), employeeEmails));

    return response;
  }

  private OffsetDateTime convertToOffsetDateTime(LocalDate date) {
    if (date == null) {
      return null;
    }
    return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
  }

  private List<TopTraineeDto> getTopTrainees(OffsetDateTime startDate, OffsetDateTime endDate,
                                              String category, String level, String technology, Integer topN, List<String> employeeEmails) {
    logger.debug("inside getTopTrainees method ::: {}",employeeEmails);
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails)
      results = metricsRepository.findTopTrainees(startDate, endDate, category, level, technology, topN);
    else{
      results = metricsRepository.findTopTrainees(startDate, endDate, category, level, technology, topN,employeeEmails);
    }

    logger.debug("got getTopTrainees::: {}",results.size());
    return results.stream().map(row -> new TopTraineeDto(
      (String) row[0],
      ((Long) row[1]).longValue(),
      row[2] != null ? ((BigDecimal) row[2]).doubleValue() : 0.0,
      ((Long) row[3]).longValue()
    )).collect(Collectors.toList());
  }

  private List<TopCourseDto> getTopRatedCourses(OffsetDateTime startDate, OffsetDateTime endDate,
                                                 String category, String level, String technology, Integer topN, List<String> employeeEmails) {
    logger.debug("inside getTopRatedCourses method ::: {}",employeeEmails);
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails) {
      results = metricsRepository.findTopRatedCourses(startDate, endDate, category, level, technology, topN);
    }
    else{
      results = metricsRepository.findTopRatedCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    }
    logger.debug("got results in getTopRatedCourses method ::: {}",results.size());
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
                                                    String category, String level, String technology, Integer topN, List<String> employeeEmails) {
    logger.debug("inside getTopEnrolledCourses method ::: {}",employeeEmails);
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails) {
      results = metricsRepository.findTopEnrolledCourses(startDate, endDate, category, level, technology, topN);
    }
    else{
      results = metricsRepository.findTopEnrolledCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    }

    //List<Object[]> results = metricsRepository.findTopEnrolledCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    logger.debug("got results in getTopEnrolledCourses method ::: {}",results.size());
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
                                                  String category, String level, String technology, Integer topN, List<String> employeeEmails) {
    logger.debug("inside getTopViewedCourses method ::: {}",employeeEmails);
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails) {
      results = metricsRepository.findTopViewedCourses(startDate, endDate, category, level, technology, topN);
    }
    else{
      results = metricsRepository.findTopViewedCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    }

    //List<Object[]> results = metricsRepository.findTopViewedCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    logger.debug("got results in getTopViewedCourses method ::: {}",results.size());
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
                                                     String category, String level, String technology, Integer topN, List<String> employeeEmails) {
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails) {
      results = metricsRepository.findTopCompletedCourses(startDate, endDate, category, level, technology, topN);
    }
    else{
      results = metricsRepository.findTopCompletedCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
    }
    //List<Object[]> results = metricsRepository.findTopCompletedCourses(startDate, endDate, category, level, technology, topN, employeeEmails);
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
                                                         String category, String level, String technology, List<String> employeeEmails) {
    List<Object[]> results = new ArrayList<>();
    if(null == employeeEmails) {
      results = metricsRepository.findUserTrainingDump(startDate, endDate, category, level, technology);
    }
    else{
      results = metricsRepository.findUserTrainingDump(startDate, endDate, category, level, technology, employeeEmails);
    }
    //List<Object[]> results = metricsRepository.findUserTrainingDump(startDate, endDate, category, level, technology, employeeEmails);
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
