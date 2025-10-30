package com.example.lms.util;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Standalone Java program to read SharePoint folder contents and write to Excel file.
 * 
 * This program:
 * 1. Reads an Excel file from local system (configurable path)
 * 2. Reads cell A1 from the 3rd worksheet (contains SharePoint folder name)
 * 3. Connects to SharePoint using Microsoft Graph API
 * 4. Reads the contents of the specified SharePoint folder
 * 5. Lists all files with their extensions in Excel column A from row 2 onwards
 * 
 * Configuration required in application.properties:
 * - graph.tenant-id: Azure AD tenant ID
 * - graph.client-id: Azure AD app client ID
 * - graph.client-secret: Azure AD app client secret
 * - graph.user-principal-name: User's email for OneDrive access
 * - graph.base-path: Base path within OneDrive/SharePoint
 * 
 * Usage: java -cp <classpath> com.example.lms.util.SharePointFolderReader
 */
public class SharePointFolderReader {

    private static final Logger logger = LoggerFactory.getLogger(SharePointFolderReader.class);
    private static final List<String> GRAPH_SCOPES = List.of("https://graph.microsoft.com/.default");
    
    private static final String EXCEL_FILE_PATH = "C:\\files\\sharepoint_folders.xlsx";
    private static final int WORKSHEET_INDEX = 2;          // 3rd worksheet (0-based index)
    private static final int FOLDER_NAME_ROW = 0;          // Row 1 (0-based index)
    private static final int FOLDER_NAME_COLUMN = 0;       // Column A (0-based index)
    private static final int OUTPUT_COLUMN = 0;            // Column A (0-based index)
    private static final int DATA_START_ROW = 1;           // Row 2 (0-based index)

    private static String tenantId;
    private static String clientId;
    private static String clientSecret;
    private static String userPrincipalName;
    private static String basePath;

    public static void main(String[] args) {
        try {
            logger.info("Starting SharePoint Folder Reader");
            
            loadConfiguration();
            
            validateConfiguration();
            
            processExcelFile();
            
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
        Properties properties = new Properties();
        
        try (InputStream input = SharePointFolderReader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            properties.load(input);
        }
        
        tenantId = properties.getProperty("graph.tenant-id");
        clientId = properties.getProperty("graph.client-id");
        clientSecret = properties.getProperty("graph.client-secret");
        userPrincipalName = properties.getProperty("graph.user-principal-name");
        basePath = properties.getProperty("graph.base-path");
        
        logger.info("Configuration loaded successfully");
    }

    /**
     * Validate that all required configuration properties are set
     */
    private static void validateConfiguration() {
        List<String> missingProperties = new ArrayList<>();
        
        if (tenantId == null || tenantId.trim().isEmpty() || tenantId.contains("your-")) {
            missingProperties.add("graph.tenant-id");
        }
        if (clientId == null || clientId.trim().isEmpty() || clientId.contains("your-")) {
            missingProperties.add("graph.client-id");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty() || clientSecret.contains("your-")) {
            missingProperties.add("graph.client-secret");
        }
        if (userPrincipalName == null || userPrincipalName.trim().isEmpty()) {
            missingProperties.add("graph.user-principal-name");
        }
        if (basePath == null || basePath.trim().isEmpty()) {
            missingProperties.add("graph.base-path");
        }
        
        if (!missingProperties.isEmpty()) {
            String message = "Missing required SharePoint configuration properties: " + 
                    String.join(", ", missingProperties);
            logger.error(message);
            throw new IllegalStateException(message);
        }
        
        logger.info("Configuration validated successfully");
    }

    /**
     * Process the Excel file: read folder name from A1 of 3rd sheet, 
     * get files from SharePoint, write to column A from row 2
     */
    private static void processExcelFile() throws IOException {
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
            
            List<String> files = listFilesInSharePointFolder(folderName);
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
     * List all files in a SharePoint/OneDrive folder using Microsoft Graph API
     */
    private static List<String> listFilesInSharePointFolder(String folderPath) {
        try {
            logger.info("Initializing Microsoft Graph client");
            
            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();

            TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                    GRAPH_SCOPES, credential);

            GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
            
            logger.info("Microsoft Graph client initialized successfully");
            
            String fullPath = constructFullPath(folderPath);
            logger.info("Full SharePoint path: {}", fullPath);
            
            DriveItemCollectionPage items = graphClient
                    .users(userPrincipalName)
                    .drive()
                    .root()
                    .itemWithPath(fullPath)
                    .children()
                    .buildRequest()
                    .get();
            
            List<String> fileNames = new ArrayList<>();
            
            if (items != null && items.getCurrentPage() != null) {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.file != null) {
                        fileNames.add(item.name);
                        logger.debug("Found file: {}", item.name);
                    }
                }
            }
            
            logger.info("Found {} files in SharePoint folder", fileNames.size());
            return fileNames;
            
        } catch (Exception e) {
            logger.error("Error accessing SharePoint folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to access SharePoint folder: " + folderPath + 
                    ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Construct the full path by combining base path and folder path
     */
    private static String constructFullPath(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            return basePath;
        }
        
        String cleanFolderPath = folderPath.trim().replaceAll("^/+|/+$", "");
        
        String cleanBasePath = basePath.trim().replaceAll("/+$", "");
        
        if (cleanFolderPath.isEmpty()) {
            return cleanBasePath;
        }
        
        return cleanBasePath + "/" + cleanFolderPath;
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
