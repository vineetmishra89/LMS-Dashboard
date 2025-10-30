package com.example.lms.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.Properties;

/**
 * Standalone Java program to append SharePoint URL to folder names in Excel file.
 * 
 * This program:
 * 1. Reads an Excel file from C:\files\training.xlsx
 * 2. Processes the "Folders" worksheet (2nd sheet)
 * 3. Reads folder names from column M (starting from row 2)
 * 4. URL-encodes the folder names and appends to SharePoint base URL
 * 5. Writes the complete URL to column N
 * 
 * Usage: java -cp <classpath> com.example.lms.util.SharePointUrlAppender
 */
public class SharePointUrlAppender {

    private static final String EXCEL_FILE_PATH = "C:\\files\\training.xlsx";
    private static final String WORKSHEET_NAME = "Folders";
    private static final int FOLDER_NAME_COLUMN = 12; // Column M (0-based index)
    private static final int URL_OUTPUT_COLUMN = 13;   // Column N (0-based index)
    private static final int DATA_START_ROW = 1;       // Row 2 (0-based index)
    private static final String NA_VALUE = "NA";

    public static void main(String[] args) {
        try {
            String sharePointBaseUrl = loadSharePointBaseUrl();
            System.out.println("SharePoint Base URL: " + sharePointBaseUrl);
            
            processExcelFile(sharePointBaseUrl);
            
            System.out.println("Excel file processing completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Load SharePoint base URL from application.properties
     */
    private static String loadSharePointBaseUrl() throws IOException {
        Properties properties = new Properties();
        
        try (InputStream input = SharePointUrlAppender.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
        }
        
        String baseUrl = properties.getProperty("sharepoint-url");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("sharepoint-url property is not configured in application.properties");
        }
        
        return baseUrl.trim();
    }

    /**
     * Process the Excel file: read column M, encode folder names, write to column N
     */
    private static void processExcelFile(String sharePointBaseUrl) throws IOException {
        File excelFile = new File(EXCEL_FILE_PATH);
        
        if (!excelFile.exists()) {
            throw new FileNotFoundException("Excel file not found at: " + EXCEL_FILE_PATH);
        }
        
        if (!excelFile.canRead() || !excelFile.canWrite()) {
            throw new IOException("Excel file is locked or not accessible: " + EXCEL_FILE_PATH);
        }
        
        Workbook workbook = null;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        
        try {
            fileInputStream = new FileInputStream(excelFile);
            workbook = new XSSFWorkbook(fileInputStream);
            
            Sheet sheet = workbook.getSheet(WORKSHEET_NAME);
            if (sheet == null) {
                throw new IllegalArgumentException("Worksheet '" + WORKSHEET_NAME + "' not found in Excel file");
            }
            
            System.out.println("Processing worksheet: " + WORKSHEET_NAME);
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Header row not found in worksheet");
            }
            
            Cell headerCell = headerRow.getCell(FOLDER_NAME_COLUMN);
            if (headerCell == null) {
                throw new IllegalArgumentException("Column M not found in header row");
            }
            
            int rowsProcessed = 0;
            int rowsWithData = 0;
            int rowsWithBlankData = 0;
            
            int lastRowNum = sheet.getLastRowNum();
            for (int rowIndex = DATA_START_ROW; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                
                Cell folderNameCell = row.getCell(FOLDER_NAME_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String folderName = getCellValueAsString(folderNameCell);
                
                Cell urlCell = row.getCell(URL_OUTPUT_COLUMN);
                if (urlCell == null) {
                    urlCell = row.createCell(URL_OUTPUT_COLUMN);
                }
                
                if (folderName == null || folderName.trim().isEmpty()) {
                    urlCell.setCellValue(NA_VALUE);
                    rowsWithBlankData++;
                } else {
                    String encodedUrl = createEncodedSharePointUrl(sharePointBaseUrl, folderName.trim());
                    urlCell.setCellValue(encodedUrl);
                    rowsWithData++;
                }
                
                rowsProcessed++;
            }
            
            fileInputStream.close();
            
            fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
            
            System.out.println("Processing complete:");
            System.out.println("  Total rows processed: " + rowsProcessed);
            System.out.println("  Rows with folder names: " + rowsWithData);
            System.out.println("  Rows with blank values (set to NA): " + rowsWithBlankData);
            
        } catch (IOException e) {
            if (e.getMessage().contains("being used by another process") || 
                e.getMessage().contains("locked")) {
                throw new IOException("Excel file is locked or open in another program. Please close it and try again.", e);
            }
            throw e;
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing input stream: " + e.getMessage());
            }
            
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing output stream: " + e.getMessage());
            }
            
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing workbook: " + e.getMessage());
            }
        }
    }

    /**
     * Create encoded SharePoint URL by appending encoded folder name to base URL
     */
    private static String createEncodedSharePointUrl(String baseUrl, String folderName) {
        String encodedUrl = UriComponentsBuilder
                .fromUriString(baseUrl)
                .pathSegment(folderName)
                .build()
                .toUriString();
        
        return encodedUrl;
    }

    /**
     * Get cell value as string, handling different cell types
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.rint(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException e2) {
                        return "";
                    }
                }
            default:
                return cell.toString();
        }
    }
}
