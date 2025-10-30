package com.example.lms.controller;

import com.example.lms.service.SharePointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public SharePointController(SharePointService sharePointService) {
        this.sharePointService = sharePointService;
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
}
