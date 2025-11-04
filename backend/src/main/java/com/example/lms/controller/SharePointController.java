package com.example.lms.controller;

import com.example.lms.dto.FolderNode;
import com.example.lms.dto.SharePointSyncResult;
import com.example.lms.service.SharePointService;
import com.example.lms.service.UserTokenSharePointService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for SharePoint/OneDrive operations.
 * Provides endpoints to interact with SharePoint folders and files.
 */
@RestController
@RequestMapping("/api/sharepoint")
@CrossOrigin
public class SharePointController {

    private static final Logger logger = LoggerFactory.getLogger(SharePointController.class);
    private final SharePointService sharePointService;
    private final UserTokenSharePointService userTokenSharePointService;

    public SharePointController(SharePointService sharePointService, 
                                UserTokenSharePointService userTokenSharePointService) {
        this.sharePointService = sharePointService;
        this.userTokenSharePointService = userTokenSharePointService;
    }

    /**
     * Lists all files in a SharePoint/OneDrive folder.
     * 
     * @param folderPath Relative path to the folder (query parameter)
     * @return List of file names with extensions
     * 
     * Example: GET /api/sharepoint/files?folderPath=Training Materials/2024
     */
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(@RequestParam(required = false, defaultValue = "") String folderPath) {
        try {
            logger.info("Received request to list files in folder: {}", folderPath);
            
            sharePointService.validateConfiguration();
            
            List<String> files = sharePointService.listFilesInFolder(folderPath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("folderPath", folderPath);
            response.put("fileCount", files.size());
            response.put("files", files);
            
            logger.info("Successfully retrieved {} files from folder: {}", files.size(), folderPath);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            logger.error("Configuration error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Configuration Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            
        } catch (Exception e) {
            logger.error("Error listing files in folder '{}': {}", folderPath, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list files");
            error.put("message", e.getMessage());
            error.put("folderPath", folderPath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Validates SharePoint configuration.
     * 
     * @return Configuration status
     * 
     * Example: GET /api/sharepoint/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateConfiguration() {
        try {
            logger.info("Validating SharePoint configuration");
            sharePointService.validateConfiguration();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "valid");
            response.put("message", "SharePoint configuration is valid");
            
            logger.info("SharePoint configuration validated successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            logger.error("Configuration validation failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "invalid");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Tests access to a SharePoint/OneDrive folder with detailed diagnostics.
     * This endpoint provides comprehensive information about the connection, authentication,
     * path normalization, and folder access to help troubleshoot issues.
     * 
     * @param folderPath Relative path to the folder to test (query parameter)
     * @return Detailed diagnostic information including configuration, resolved paths, and file count
     * 
     * Example: GET /api/sharepoint/test-access?folderPath=Training Materials/2024
     */
    @GetMapping("/test-access")
    public ResponseEntity<?> testFolderAccess(@RequestParam(required = false, defaultValue = "") String folderPath) {
        try {
            logger.info("Received request to test folder access for: {}", folderPath);
            
            String diagnostics = sharePointService.testFolderAccess(folderPath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("diagnostics", diagnostics);
            response.put("folderPath", folderPath);
            
            logger.info("Folder access test completed for: {}", folderPath);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during folder access test for '{}': {}", folderPath, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test failed");
            error.put("message", e.getMessage());
            error.put("folderPath", folderPath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lists all document libraries (drives) available on the SharePoint site.
     * Only works in SharePoint site mode (graph.mode=site).
     * Helps discover drive IDs for configuration.
     * 
     * @return List of drives with their IDs, names, and URLs
     * 
     * Example: GET /api/sharepoint/site/drives
     */
    @GetMapping("/site/drives")
    public ResponseEntity<?> listSiteDrives() {
        try {
            logger.info("Received request to list SharePoint site drives");
            
            String drivesInfo = sharePointService.listSiteDrives();
            
            Map<String, Object> response = new HashMap<>();
            response.put("drives", drivesInfo);
            
            logger.info("Successfully listed SharePoint site drives");
            return ResponseEntity.ok(response);
            
        } catch (UnsupportedOperationException e) {
            logger.warn("listSiteDrives called in OneDrive mode: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unsupported Operation");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Error listing SharePoint site drives: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list drives");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lists all folders and files recursively from a SharePoint/OneDrive folder using user's bearer token.
     * Uses configured drive ID and folder ID from application.properties.
     * This endpoint uses delegated permissions (user context) from the Authorization header.
     * 
     * @param httpRequest HTTP servlet request to read Authorization header
     * @return Recursive tree structure of folders and files
     * 
     * Example: POST /api/sharepoint/list-with-user-token
     * Headers: Authorization: Bearer <your-token>
     * 
     * Configuration required in application.properties:
     * - graph.user.drive-id: The SharePoint drive ID
     * - graph.user.root-folder-id: The root folder item ID
     */
    @PostMapping("/list-with-user-token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> listWithUserToken(HttpServletRequest httpRequest) {
        try {
            logger.info("Received request to list folders/files with user token using configured IDs");
            
            String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid Authorization header");
                error.put("message", "Authorization header must start with 'Bearer '");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String bearerToken = authorization.substring(7).trim();
            
            FolderNode result = userTokenSharePointService.listFoldersAndFilesRecursivelyFromIds(bearerToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("folderStructure", result);
            
            logger.info("Successfully listed folders and files recursively from configured IDs");
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            logger.error("Configuration error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Configuration Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Error listing folders/files with user token: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list folders and files");
            error.put("message", e.getMessage());
            
            if (e.getMessage() != null && 
                (e.getMessage().contains("401") || 
                 e.getMessage().contains("Unauthorized") ||
                 e.getMessage().contains("Invalid token"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Syncs SharePoint folders and files to LMS database.
     * Matches folders by name or URL, processes .mp4 files, and inserts/updates course modules.
     * This endpoint uses delegated permissions (user context) from the Authorization header.
     * 
     * @param httpRequest HTTP servlet request to read Authorization header
     * @param dryRun If true, performs validation without database changes (default: false)
     * @return Sync operation results with counts and errors
     * 
     * Example: POST /api/sharepoint/sync-modules-with-user-token?dryRun=true
     * Headers: Authorization: Bearer <your-token>
     * 
     * Configuration required in application.properties:
     * - graph.user.drive-id: The SharePoint drive ID
     * - graph.user.root-folder-id: The root folder item ID
     */
    @PostMapping("/sync-modules-with-user-token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> syncModulesWithUserToken(
            HttpServletRequest httpRequest,
            @RequestParam(required = false, defaultValue = "false") boolean dryRun) {
        try {
            logger.info("Received request to sync modules with user token (dryRun={})", dryRun);
            
            String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid Authorization header");
                error.put("message", "Authorization header must start with 'Bearer '");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String bearerToken = authorization.substring(7).trim();
            
            SharePointSyncResult result = userTokenSharePointService.syncModulesFromSharePoint(bearerToken, dryRun);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getErrors().isEmpty());
            response.put("result", result);
            
            if (result.getErrors().isEmpty()) {
                logger.info("Successfully synced modules: foldersProcessed={}, coursesMatched={}, modulesInserted={}, modulesUpdated={}",
                        result.getFoldersProcessed(), result.getCoursesMatched(), result.getModulesInserted(), result.getModulesUpdated());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Sync completed with errors: {}", result.getErrors());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (IllegalStateException e) {
            logger.error("Configuration error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Configuration Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            
        } catch (Exception e) {
            logger.error("Error syncing modules with user token: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sync modules");
            error.put("message", e.getMessage());
            
            if (e.getMessage() != null && 
                (e.getMessage().contains("401") || 
                 e.getMessage().contains("Unauthorized") ||
                 e.getMessage().contains("Invalid token"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
