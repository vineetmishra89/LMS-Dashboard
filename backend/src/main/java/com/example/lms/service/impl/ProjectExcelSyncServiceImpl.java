package com.example.lms.service.impl;

import com.example.lms.domain.EmployeeDetails;
import com.example.lms.domain.ProjectDetails;
import com.example.lms.dto.ProjectExcelSyncResult;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.repo.ProjectRepository;
import com.example.lms.service.GraphUserLookupService;
import com.example.lms.service.ProjectExcelSyncService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ProjectExcelSyncServiceImpl implements ProjectExcelSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectExcelSyncServiceImpl.class);
    
    private static final String ERROR_COLUMN_HEADER = "ERROR_LOG";
    private static final int ERROR_COLUMN_INDEX = 11; // Column L (0-indexed)
    
    @Value("${project.sync.excel.file}")
    private String excelFilePath;
    
    @Value("${project.sync.excel.sheet:Sheet1}")
    private String sheetName;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EmployeeDetailsRepository employeeDetailsRepository;
    
    @Autowired
    private GraphUserLookupService graphUserLookupService;
    
    @Override
    @Transactional
    public ProjectExcelSyncResult syncFromExcel(String bearerToken) {
        logger.info("Starting project sync from Excel file: {}", excelFilePath);
        
        ProjectExcelSyncResult result = ProjectExcelSyncResult.builder()
                .excelFilePath(excelFilePath)
                .totalRows(0)
                .projectsInserted(0)
                .projectsSkippedExisting(0)
                .employeesUpdated(0)
                .graphLookupsSucceeded(0)
                .graphLookupsFailed(0)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
        
        Workbook workbook = null;
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(excelFilePath);
            workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(sheetName);
            
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
                logger.warn("Sheet '{}' not found, using first sheet: {}", sheetName, sheet.getSheetName());
            }
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                String error = "Excel file has no header row";
                logger.error(error);
                result.getErrors().add(error);
                return result;
            }
            
            Map<String, Integer> headerIndices = parseHeaders(headerRow);
            
            if (headerRow.getCell(ERROR_COLUMN_INDEX) == null) {
                Cell errorHeaderCell = headerRow.createCell(ERROR_COLUMN_INDEX);
                errorHeaderCell.setCellValue(ERROR_COLUMN_HEADER);
            }
            
            String validationError = validateHeaders(headerIndices);
            if (validationError != null) {
                logger.error(validationError);
                result.getErrors().add(validationError);
                return result;
            }
            
            Set<String> seenProjects = new HashSet<>();
            
            int lastRowNum = sheet.getLastRowNum();
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                result.setTotalRows(result.getTotalRows() + 1);
                
                try {
                    processRow(row, rowIndex, headerIndices, bearerToken, seenProjects, result);
                } catch (Exception e) {
                    String errorMsg = String.format("Row %d: Unexpected error - %s", rowIndex + 1, e.getMessage());
                    logger.error(errorMsg, e);
                    result.getErrors().add(errorMsg);
                    writeErrorToCell(row, ERROR_COLUMN_INDEX, errorMsg);
                }
            }
            
            fis.close();
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                logger.info("Excel file updated with error logs");
            }
            
            logger.info("Project sync completed. Total rows: {}, Projects inserted: {}, Projects skipped: {}, Employees updated: {}",
                    result.getTotalRows(), result.getProjectsInserted(), result.getProjectsSkippedExisting(), result.getEmployeesUpdated());
            
        } catch (IOException e) {
            String error = String.format("Failed to read Excel file '%s': %s", excelFilePath, e.getMessage());
            logger.error(error, e);
            result.getErrors().add(error);
        } catch (Exception e) {
            String error = String.format("Unexpected error during sync: %s", e.getMessage());
            logger.error(error, e);
            result.getErrors().add(error);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                logger.error("Error closing Excel file: {}", e.getMessage(), e);
            }
        }
        
        return result;
    }
    
    private Map<String, Integer> parseHeaders(Row headerRow) {
        Map<String, Integer> headerIndices = new HashMap<>();
        
        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING) {
                String header = normalizeHeader(cell.getStringCellValue());
                headerIndices.put(header, cell.getColumnIndex());
                logger.debug("Found header '{}' at column {}", header, cell.getColumnIndex());
            }
        }
        
        return headerIndices;
    }
    
    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.trim().toUpperCase().replace(" ", "_").replace("__", "_");
    }
    
    private String validateHeaders(Map<String, Integer> headerIndices) {
        List<String> requiredHeaders = Arrays.asList("EMP_NAME", "PROJECT", "PM", "ADM");
        List<String> missingHeaders = new ArrayList<>();
        
        for (String required : requiredHeaders) {
            if (!headerIndices.containsKey(required)) {
                missingHeaders.add(required);
            }
        }
        
        if (!missingHeaders.isEmpty()) {
            return "Missing required headers: " + String.join(", ", missingHeaders);
        }
        
        return null;
    }
    
    private void processRow(Row row, int rowIndex, Map<String, Integer> headerIndices, 
                           String bearerToken, Set<String> seenProjects, ProjectExcelSyncResult result) {
        
        StringBuilder rowErrors = new StringBuilder();
        
        try {
            String empName = getCellValueAsString(row, headerIndices.get("EMP_NAME"));
            String projectName = getCellValueAsString(row, headerIndices.get("PROJECT"));
            String pmName = getCellValueAsString(row, headerIndices.get("PM"));
            String admName = getCellValueAsString(row, headerIndices.get("ADM"));
            
            logger.debug("Row {}: Processing - Project: {}, PM: {}, ADM: {}, Employee: {}", 
                    rowIndex + 1, projectName, pmName, admName, empName);
            
            if (projectName == null || projectName.trim().isEmpty()) {
                String error = "Project name is empty";
                logger.warn("Row {}: {}", rowIndex + 1, error);
                rowErrors.append(error).append("; ");
                writeErrorToCell(row, ERROR_COLUMN_INDEX, error);
                return;
            }
            
            projectName = projectName.trim();
            
            boolean projectAlreadyExists = projectRepository.existsById(projectName);
            boolean projectSeenInThisRun = seenProjects.contains(projectName);
            
            if (projectAlreadyExists || projectSeenInThisRun) {
                logger.debug("Row {}: Project '{}' already exists, skipping project insert", rowIndex + 1, projectName);
                result.setProjectsSkippedExisting(result.getProjectsSkippedExisting() + 1);
            } else {
                try {
                    insertProject(projectName, pmName, admName, bearerToken, result, rowErrors);
                    seenProjects.add(projectName);
                    result.setProjectsInserted(result.getProjectsInserted() + 1);
                    logger.info("Row {}: Successfully inserted project '{}'", rowIndex + 1, projectName);
                } catch (Exception e) {
                    String error = String.format("Failed to insert project: %s", e.getMessage());
                    logger.error("Row {}: {}", rowIndex + 1, error, e);
                    rowErrors.append(error).append("; ");
                }
            }
            
            if (empName != null && !empName.trim().isEmpty()) {
                try {
                    int updatedCount = updateEmployeeProject(empName.trim(), projectName, rowIndex);
                    result.setEmployeesUpdated(result.getEmployeesUpdated() + updatedCount);
                    
                    if (updatedCount == 0) {
                        String warning = String.format("No employee found with name '%s'", empName);
                        logger.warn("Row {}: {}", rowIndex + 1, warning);
                        rowErrors.append(warning).append("; ");
                    } else {
                        logger.debug("Row {}: Updated {} employee(s) with name '{}'", rowIndex + 1, updatedCount, empName);
                    }
                } catch (Exception e) {
                    String error = String.format("Failed to update employee: %s", e.getMessage());
                    logger.error("Row {}: {}", rowIndex + 1, error, e);
                    rowErrors.append(error).append("; ");
                }
            }
            
            if (rowErrors.length() > 0) {
                writeErrorToCell(row, ERROR_COLUMN_INDEX, rowErrors.toString());
            } else {
                writeErrorToCell(row, ERROR_COLUMN_INDEX, "");
            }
            
        } catch (Exception e) {
            String error = String.format("Row processing error: %s", e.getMessage());
            logger.error("Row {}: {}", rowIndex + 1, error, e);
            rowErrors.append(error);
            writeErrorToCell(row, ERROR_COLUMN_INDEX, rowErrors.toString());
        }
    }
    
    private void insertProject(String projectName, String pmName, String admName, 
                              String bearerToken, ProjectExcelSyncResult result, StringBuilder rowErrors) {
        
        ProjectDetails project = new ProjectDetails();
        project.setProjectName(projectName);
        project.setProjActiveFlag("Y");
        project.setCreatedBy("EXCEL_SYNC");
        project.setUpdatedBy("EXCEL_SYNC");
        project.setCreatedTs(OffsetDateTime.now());
        project.setUpdatedTs(OffsetDateTime.now());
        
        if (pmName != null && !pmName.trim().isEmpty()) {
            Optional<String> pmEmail = graphUserLookupService.lookupEmailByDisplayName(bearerToken, pmName.trim());
            if (pmEmail.isPresent()) {
                project.setPmEmailId(pmEmail.get());
                result.setGraphLookupsSucceeded(result.getGraphLookupsSucceeded() + 1);
                logger.debug("PM '{}' resolved to email: {}", pmName, pmEmail.get());
            } else {
                project.setPmEmailId(pmName.trim());
                result.setGraphLookupsFailed(result.getGraphLookupsFailed() + 1);
                String warning = String.format("PM '%s' not found in Graph API, using Excel value", pmName);
                logger.warn(warning);
                rowErrors.append(warning).append("; ");
            }
        }
        
        if (admName != null && !admName.trim().isEmpty()) {
            Optional<String> admEmail = graphUserLookupService.lookupEmailByDisplayName(bearerToken, admName.trim());
            if (admEmail.isPresent()) {
                project.setAdmEmailId(admEmail.get());
                result.setGraphLookupsSucceeded(result.getGraphLookupsSucceeded() + 1);
                logger.debug("ADM '{}' resolved to email: {}", admName, admEmail.get());
            } else {
                project.setAdmEmailId(admName.trim());
                result.setGraphLookupsFailed(result.getGraphLookupsFailed() + 1);
                String warning = String.format("ADM '%s' not found in Graph API, using Excel value", admName);
                logger.warn(warning);
                rowErrors.append(warning).append("; ");
            }
        }
        
        projectRepository.save(project);
        logger.info("Inserted project: {} with PM: {}, ADM: {}", projectName, project.getPmEmailId(), project.getAdmEmailId());
    }
    
    private int updateEmployeeProject(String empName, String projectName, int rowIndex) {
        List<EmployeeDetails> employees = employeeDetailsRepository.findByEmpNameIgnoreCase(empName);
        
        if (employees.isEmpty()) {
            logger.debug("Row {}: No employee found with name '{}'", rowIndex + 1, empName);
            return 0;
        }
        
        int updatedCount = 0;
        for (EmployeeDetails employee : employees) {
            employee.setProjectName(projectName);
            employee.setUpdatedBy("EXCEL_SYNC");
            employee.setUpdatedTs(OffsetDateTime.now());
            employeeDetailsRepository.save(employee);
            updatedCount++;
            logger.debug("Updated employee {} ({}) with project '{}'", employee.getEmpName(), employee.getEmailId(), projectName);
        }
        
        if (employees.size() > 1) {
            logger.info("Row {}: Updated {} employees with name '{}'", rowIndex + 1, updatedCount, empName);
        }
        
        return updatedCount;
    }
    
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException e2) {
                        return null;
                    }
                }
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }
    
    private void writeErrorToCell(Row row, int columnIndex, String errorMessage) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(errorMessage);
    }
}
