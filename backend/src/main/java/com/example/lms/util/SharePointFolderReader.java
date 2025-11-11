package com.example.lms.util;

import com.example.lms.service.SharePointService;
import com.example.lms.service.impl.GraphClientProvider;
import com.example.lms.service.impl.OneDriveSharePointServiceImpl;
import com.example.lms.service.impl.SharePointSiteServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Standalone Java program to read SharePoint folder contents and write to Excel file.
 * 
 * This program:
 * 1. Reads an Excel file from local system (configurable path)
 * 2. Reads cell A1 from the 3rd worksheet (contains SharePoint folder path or URL)
 * 3. Connects to SharePoint using Microsoft Graph API
 * 4. Reads the contents of the specified SharePoint folder
 * 5. Lists all files with their extensions in Excel column A from row 2 onwards
 * 
 * Cell A1 Input Formats Supported:
 * - Full SharePoint URL: https://irissoft-my.sharepoint.com/:f:/r/personal/user/Documents/Folder
 * - OneDrive URL with query: https://.../_layouts/15/onedrive.aspx?id=%2Fpersonal%2F...
 * - Relative path: "Folder Name" or "Subfolder/Nested Folder"
 * 
 * Configuration required in application.properties:
 * - graph.mode: "onedrive" (default) or "site"
 * - graph.tenant-id: Azure AD tenant ID
 * - graph.client-id: Azure AD app client ID
 * - graph.client-secret: Azure AD app client secret
 * 
 * For OneDrive mode:
 * - graph.user-principal-name: User's email for OneDrive access
 * - graph.base-path: Base path within OneDrive
 * 
 * For SharePoint site mode:
 * - graph.site-hostname: SharePoint site hostname
 * - graph.site-path: SharePoint site path
 * - graph.drive-id: (Optional) Specific drive ID
 * - graph.base-path: Base path within the drive
 * 
 * Usage: java -cp <classpath> com.example.lms.util.SharePointFolderReader
 */
public class SharePointFolderReader {

    private static final Logger logger = LoggerFactory.getLogger(SharePointFolderReader.class);
    
    private static final String EXCEL_FILE_PATH = "C:\\files\\sharepoint_folders.xlsx";
    private static final int WORKSHEET_INDEX = 2;          // 3rd worksheet (0-based index)
    private static final int FOLDER_NAME_ROW = 0;          // Row 1 (0-based index)
    private static final int FOLDER_NAME_COLUMN = 0;       // Column A (0-based index)
    private static final int OUTPUT_COLUMN = 0;            // Column A (0-based index)
    private static final int DATA_START_ROW = 1;           // Row 2 (0-based index)

    private static Properties properties;

    public static void main(String[] args) {
        try {
            logger.info("Starting SharePoint Folder Reader");
            
            loadConfiguration();
            
            SharePointService sharePointService = createSharePointService();
            
            sharePointService.validateConfiguration();
            
            processExcelFile(sharePointService);
            
            logger.info("SharePoint folder reading completed successfully!");
            
        } catch (Exception e) {
            logger.error("Error processing SharePoint folder: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Load configuration from application.properties
     */
    private static void loadConfiguration() throws IOException {
        properties = new Properties();
        
        try (InputStream input = SharePointFolderReader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
        }
        
        logger.info("Configuration loaded successfully");
    }

    /**
     * Creates the appropriate SharePoint service implementation based on graph.mode property.
     * 
     * @return SharePointService implementation (OneDrive or SharePoint site)
     */
    private static SharePointService createSharePointService() {
        String mode = properties.getProperty("graph.mode", "onedrive").trim().toLowerCase();
        logger.info("Creating SharePoint service for mode: {}", mode);
        
        GraphClientProvider graphClientProvider = new GraphClientProvider();
        
        setField(graphClientProvider, "tenantId", properties.getProperty("graph.tenant-id"));
        setField(graphClientProvider, "clientId", properties.getProperty("graph.client-id"));
        setField(graphClientProvider, "clientSecret", properties.getProperty("graph.client-secret"));
        
        SharePointService service;
        
        if ("site".equals(mode)) {
            SharePointSiteServiceImpl siteService = new SharePointSiteServiceImpl(graphClientProvider);
            setField(siteService, "siteHostname", properties.getProperty("graph.site-hostname"));
            setField(siteService, "sitePath", properties.getProperty("graph.site-path"));
            setField(siteService, "driveId", properties.getProperty("graph.drive-id"));
            setField(siteService, "basePath", properties.getProperty("graph.base-path"));
            service = siteService;
            logger.info("Created SharePointSiteServiceImpl");
        } else {
            OneDriveSharePointServiceImpl oneDriveService = new OneDriveSharePointServiceImpl(graphClientProvider);
            setField(oneDriveService, "userPrincipalName", properties.getProperty("graph.user-principal-name"));
            setField(oneDriveService, "basePath", properties.getProperty("graph.base-path"));
            service = oneDriveService;
            logger.info("Created OneDriveSharePointServiceImpl");
        }
        
        return service;
    }

    /**
     * Sets a private field value using reflection.
     * Used to manually inject configuration values outside of Spring context.
     */
    private static void setField(Object target, String fieldName, String value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            try {
                java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                logger.warn("Could not set field {}: {}", fieldName, ex.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Could not set field {}: {}", fieldName, e.getMessage());
        }
    }

    /**
     * Process the Excel file: read folder name from A1 of 3rd sheet, 
     * get files from SharePoint, write to column A from row 2
     */
    private static void processExcelFile(SharePointService sharePointService) throws IOException {
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
            
            if (workbook.getNumberOfSheets() < 3) {
                throw new IllegalArgumentException("Excel file does not have a 3rd worksheet. Found only " + 
                        workbook.getNumberOfSheets() + " worksheet(s)");
            }
            
            Sheet sheet = workbook.getSheetAt(WORKSHEET_INDEX);
            logger.info("Processing worksheet: {} (index {})", sheet.getSheetName(), WORKSHEET_INDEX);
            
            Row folderNameRow = sheet.getRow(FOLDER_NAME_ROW);
            if (folderNameRow == null) {
                throw new IllegalArgumentException("Row 1 not found in 3rd worksheet");
            }
            
            Cell folderNameCell = folderNameRow.getCell(FOLDER_NAME_COLUMN, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String folderName = getCellValueAsString(folderNameCell);
            
            if (folderName == null || folderName.trim().isEmpty()) {
                throw new IllegalArgumentException("Cell A1 in 3rd worksheet is empty. Please provide a SharePoint folder name.");
            }
            
            folderName = folderName.trim();
            logger.info("Reading files from SharePoint folder: {}", folderName);
            
            List<String> files = sharePointService.listFilesInFolder(folderName);
            logger.info("Retrieved {} files from SharePoint folder", files.size());
            
            clearColumnData(sheet, OUTPUT_COLUMN, DATA_START_ROW);
            
            writeFilesToExcel(sheet, files);
            
            fileInputStream.close();
            
            fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
            
            logger.info("Successfully wrote {} files to Excel column A", files.size());
            
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
                logger.error("Error closing input stream: {}", e.getMessage());
            }
            
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                logger.error("Error closing output stream: {}", e.getMessage());
            }
            
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                logger.error("Error closing workbook: {}", e.getMessage());
            }
        }
    }

    /**
     * Clear existing data in a column from a specified row onwards
     */
    private static void clearColumnData(Sheet sheet, int columnIndex, int startRow) {
        int lastRowNum = sheet.getLastRowNum();
        for (int rowIndex = startRow; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null) {
                    row.removeCell(cell);
                }
            }
        }
        logger.debug("Cleared column {} from row {} onwards", columnIndex, startRow + 1);
    }

    /**
     * Write file names to Excel column A starting from row 2
     */
    private static void writeFilesToExcel(Sheet sheet, List<String> files) {
        for (int i = 0; i < files.size(); i++) {
            int rowIndex = DATA_START_ROW + i;
            Row row = sheet.getRow(rowIndex);
            
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            
            Cell cell = row.getCell(OUTPUT_COLUMN);
            if (cell == null) {
                cell = row.createCell(OUTPUT_COLUMN);
            }
            
            cell.setCellValue(files.get(i));
        }
        
        logger.debug("Wrote {} files to column A starting from row {}", files.size(), DATA_START_ROW + 1);
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
