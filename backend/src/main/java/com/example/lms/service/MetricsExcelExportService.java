package com.example.lms.service;

import com.example.lms.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class MetricsExcelExportService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public byte[] generateMetricsExcel(MetricsResponseDto metricsData) throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dataStyle = createDataStyle(workbook);

      createTopTraineesSheet(workbook, metricsData.getTopTrainees(), headerStyle, dataStyle);
      createTopRatedCoursesSheet(workbook, metricsData.getTopRatedCourses(), headerStyle, dataStyle);
      createTopEnrolledCoursesSheet(workbook, metricsData.getTopEnrolledCourses(), headerStyle, dataStyle);
      createTopViewedCoursesSheet(workbook, metricsData.getTopViewedCourses(), headerStyle, dataStyle);
      createTopCompletedCoursesSheet(workbook, metricsData.getTopCompletedCourses(), headerStyle, dataStyle);
      createUserTrainingDumpSheet(workbook, metricsData.getUserTrainingDump(), headerStyle, dataStyle);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

  private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
  }

  private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private void createTopTraineesSheet(Workbook workbook, java.util.List<TopTraineeDto> data, 
                                       CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("Top Trainees");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"User ID", "Completed Courses", "Average Progress (%)", "Total Enrollments"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (TopTraineeDto trainee : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(trainee.getUserId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(trainee.getCompletedCourses());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(trainee.getAverageProgress());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(trainee.getTotalEnrollments());
      cell3.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void createTopRatedCoursesSheet(Workbook workbook, java.util.List<TopCourseDto> data, 
                                           CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("Top Rated Courses");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"Training ID", "Topic", "Category", "Level", "Rating", "Enrollment Count"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (TopCourseDto course : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(course.getTrainingId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(course.getTopic());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(course.getCategory());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(course.getLevel());
      cell3.setCellStyle(dataStyle);
      
      Cell cell4 = row.createCell(4);
      cell4.setCellValue(course.getRating() != null?course.getRating().doubleValue(): null);
      cell4.setCellStyle(dataStyle);
      
      Cell cell5 = row.createCell(5);
      cell5.setCellValue(course.getEnrollmentCount());
      cell5.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void createTopEnrolledCoursesSheet(Workbook workbook, java.util.List<TopCourseDto> data, 
                                              CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("Top Enrolled Courses");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"Training ID", "Topic", "Category", "Level", "Rating", "Enrollment Count"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (TopCourseDto course : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(course.getTrainingId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(course.getTopic());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(course.getCategory());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(course.getLevel());
      cell3.setCellStyle(dataStyle);
      
      Cell cell4 = row.createCell(4);
      cell4.setCellValue(course.getRating() != null?course.getRating().doubleValue(): null);
      cell4.setCellStyle(dataStyle);
      
      Cell cell5 = row.createCell(5);
      cell5.setCellValue(course.getEnrollmentCount());
      cell5.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void createTopViewedCoursesSheet(Workbook workbook, java.util.List<TopCourseDto> data, 
                                            CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("Top Viewed Courses");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"Training ID", "Topic", "Category", "Level", "Rating", "Enrollment Count", "View Count"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (TopCourseDto course : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(course.getTrainingId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(course.getTopic());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(course.getCategory());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(course.getLevel());
      cell3.setCellStyle(dataStyle);
      
      Cell cell4 = row.createCell(4);
      cell4.setCellValue(course.getRating() != null?course.getRating().doubleValue(): null);
      cell4.setCellStyle(dataStyle);
      
      Cell cell5 = row.createCell(5);
      cell5.setCellValue(course.getEnrollmentCount());
      cell5.setCellStyle(dataStyle);
      
      Cell cell6 = row.createCell(6);
      cell6.setCellValue(course.getViewCount());
      cell6.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void createTopCompletedCoursesSheet(Workbook workbook, java.util.List<TopCourseDto> data, 
                                               CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("Top Completed Courses");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"Training ID", "Topic", "Category", "Level", "Rating", "Enrollment Count", "Completed Count"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (TopCourseDto course : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(course.getTrainingId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(course.getTopic());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(course.getCategory());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(course.getLevel());
      cell3.setCellStyle(dataStyle);
      
      Cell cell4 = row.createCell(4);
      cell4.setCellValue(course.getRating() != null?course.getRating().doubleValue(): null);
      cell4.setCellStyle(dataStyle);
      
      Cell cell5 = row.createCell(5);
      cell5.setCellValue(course.getEnrollmentCount());
      cell5.setCellStyle(dataStyle);
      
      Cell cell6 = row.createCell(6);
      cell6.setCellValue(course.getCompletedCount());
      cell6.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  private void createUserTrainingDumpSheet(Workbook workbook, java.util.List<UserTrainingDumpDto> data, 
                                            CellStyle headerStyle, CellStyle dataStyle) {
    Sheet sheet = workbook.createSheet("User Training Dump");
    
    Row headerRow = sheet.createRow(0);
    String[] headers = {"User ID", "Training ID", "Training Topic", "Category", "Level", 
                        "Status", "Progress (%)", "Enrolled Date", "Started Date", "Last Accessed Date"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int rowNum = 1;
    for (UserTrainingDumpDto dump : data) {
      Row row = sheet.createRow(rowNum++);
      
      Cell cell0 = row.createCell(0);
      cell0.setCellValue(dump.getUserId());
      cell0.setCellStyle(dataStyle);
      
      Cell cell1 = row.createCell(1);
      cell1.setCellValue(dump.getTrainingId());
      cell1.setCellStyle(dataStyle);
      
      Cell cell2 = row.createCell(2);
      cell2.setCellValue(dump.getTrainingTopic());
      cell2.setCellStyle(dataStyle);
      
      Cell cell3 = row.createCell(3);
      cell3.setCellValue(dump.getCategory());
      cell3.setCellStyle(dataStyle);
      
      Cell cell4 = row.createCell(4);
      cell4.setCellValue(dump.getLevel());
      cell4.setCellStyle(dataStyle);
      
      Cell cell5 = row.createCell(5);
      cell5.setCellValue(dump.getStatus());
      cell5.setCellStyle(dataStyle);
      
      Cell cell6 = row.createCell(6);
      cell6.setCellValue(dump.getProgressPercent());
      cell6.setCellStyle(dataStyle);
      
      Cell cell7 = row.createCell(7);
      cell7.setCellValue(dump.getEnrolledTs() != null ? dump.getEnrolledTs().format(DATE_TIME_FORMATTER) : "");
      cell7.setCellStyle(dataStyle);
      
      Cell cell8 = row.createCell(8);
      cell8.setCellValue(dump.getStartedTs() != null ? dump.getStartedTs().format(DATE_TIME_FORMATTER) : "");
      cell8.setCellStyle(dataStyle);
      
      Cell cell9 = row.createCell(9);
      cell9.setCellValue(dump.getLastAccessedTs() != null ? dump.getLastAccessedTs().format(DATE_TIME_FORMATTER) : "");
      cell9.setCellStyle(dataStyle);
    }

    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
  }
}
