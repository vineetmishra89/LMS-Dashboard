package com.example.lms.service.impl;

import com.example.lms.service.SharePointService;
import com.example.lms.util.PathUtils;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SharePoint service implementation for OneDrive for Business.
 * Uses Microsoft Graph API to access user's personal OneDrive folders.
 * 
 * Configuration properties:
 * - graph.mode: Must be set to "onedrive" (or omitted, as this is the default)
 * - graph.tenant-id: Azure AD tenant ID
 * - graph.client-id: Azure AD app client ID
 * - graph.client-secret: Azure AD app client secret
 * - graph.user-principal-name: User's email for OneDrive access
 * - graph.base-path: Base path within OneDrive
 * 
 * Graph API endpoint pattern: /users/{userPrincipalName}/drive/root:/{path}:/children
 */
@Service
@ConditionalOnProperty(name = "graph.mode", havingValue = "onedrive", matchIfMissing = true)
public class OneDriveSharePointServiceImpl implements SharePointService {

    private static final Logger logger = LoggerFactory.getLogger(OneDriveSharePointServiceImpl.class);

    private final GraphClientProvider graphClientProvider;

    @Value("${graph.user-principal-name}")
    private String userPrincipalName;

    @Value("${graph.base-path}")
    private String basePath;

    public OneDriveSharePointServiceImpl(GraphClientProvider graphClientProvider) {
        this.graphClientProvider = graphClientProvider;
        logger.info("OneDriveSharePointServiceImpl initialized (OneDrive for Business mode)");
    }

    @Override
    public List<String> listFilesInFolder(String folderPath) {
        try {
            logger.info("Listing files in OneDrive folder (raw input): {}", folderPath);
            
            String normalizedPath = normalizeFolderInput(folderPath);
            logger.info("Normalized folder path: {}", normalizedPath);
            
            String fullPath = PathUtils.combine(basePath, normalizedPath);
            logger.info("Resolved OneDrive path: {}", fullPath);
            
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            DriveItemCollectionPage items = client
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
            
            logger.info("Found {} files in OneDrive folder: {}", fileNames.size(), normalizedPath);
            return fileNames;
            
        } catch (Exception e) {
            logger.error("Error listing files in OneDrive folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list files in OneDrive folder: " + folderPath + 
                    ". Error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Normalizes folder input to handle both SharePoint web URLs and relative paths.
     * 
     * Supports:
     * - Full SharePoint URLs like: https://irissoft-my.sharepoint.com/:f:/r/personal/user/Documents/Folder
     * - OneDrive URLs with query params: https://.../_layouts/15/onedrive.aspx?id=%2Fpersonal%2F...
     * - Relative paths like: "Folder Name" or "Subfolder/Nested Folder"
     * 
     * @param folderInput Raw folder input from user (URL or relative path)
     * @return Normalized relative path (decoded, without personal/ prefix)
     */
    private String normalizeFolderInput(String folderInput) {
        if (folderInput == null || folderInput.trim().isEmpty()) {
            return "";
        }
        
        folderInput = folderInput.trim();
        
        if (!folderInput.startsWith("http://") && !folderInput.startsWith("https://")) {
            return folderInput;
        }
        
        try {
            URI uri = new URI(folderInput);
            String extractedPath = null;
            
            if (uri.getPath().contains("/:f:/")) {
                String path = uri.getPath();
                int fIndex = path.indexOf("/:f:/");
                
                int rIndex = path.indexOf("/r/", fIndex);
                if (rIndex != -1) {
                    extractedPath = path.substring(rIndex + 3); // Skip "/r/"
                } else {
                    extractedPath = path.substring(fIndex + 5); // Skip "/:f:/"
                }
                
                extractedPath = URLDecoder.decode(extractedPath, StandardCharsets.UTF_8);
                logger.debug("Extracted path from /:f:/r/ format: {}", extractedPath);
            }
            else if (uri.getQuery() != null && uri.getQuery().contains("id=")) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        String idValue = param.substring(3); // Skip "id="
                        extractedPath = URLDecoder.decode(idValue, StandardCharsets.UTF_8);
                        
                        if (extractedPath.startsWith("/")) {
                            extractedPath = extractedPath.substring(1);
                        }
                        logger.debug("Extracted path from query param: {}", extractedPath);
                        break;
                    }
                }
            }
            else {
                extractedPath = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
                if (extractedPath.startsWith("/")) {
                    extractedPath = extractedPath.substring(1);
                }
                logger.debug("Extracted path from URI path: {}", extractedPath);
            }
            
            if (extractedPath == null || extractedPath.isEmpty()) {
                logger.warn("Could not extract path from URL, using original input");
                return folderInput;
            }
            
            if (extractedPath.startsWith("personal/")) {
                int secondSlash = extractedPath.indexOf('/', 9); // Find slash after "personal/"
                if (secondSlash != -1) {
                    extractedPath = extractedPath.substring(secondSlash + 1);
                    logger.debug("Stripped personal/ prefix, result: {}", extractedPath);
                }
            }
            
            if (basePath != null && !basePath.isEmpty() && extractedPath.startsWith(basePath)) {
                String relative = extractedPath.substring(basePath.length());
                relative = relative.replaceFirst("^/+", "");
                logger.debug("Stripped basePath, relative path: {}", relative);
                return relative;
            }
            
            return extractedPath;
            
        } catch (Exception e) {
            logger.warn("Error parsing SharePoint URL, using original input: {}", e.getMessage());
            return folderInput;
        }
    }

    @Override
    public void validateConfiguration() {
        List<String> missingProperties = new ArrayList<>();
        
        try {
            graphClientProvider.validateAuthConfiguration();
        } catch (IllegalStateException e) {
            missingProperties.add(e.getMessage());
        }
        
        if (userPrincipalName == null || userPrincipalName.trim().isEmpty()) {
            missingProperties.add("graph.user-principal-name");
        }
        if (basePath == null || basePath.trim().isEmpty()) {
            missingProperties.add("graph.base-path");
        }
        
        if (!missingProperties.isEmpty()) {
            String message = "Missing required OneDrive configuration properties: " + 
                    String.join(", ", missingProperties);
            logger.error(message);
            throw new IllegalStateException(message);
        }
        
        logger.info("OneDrive configuration validated successfully");
    }
    
    @Override
    public String testFolderAccess(String folderPath) {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== SharePoint OneDrive Folder Access Test ===\n\n");
        
        try {
            diagnostics.append("Configuration:\n");
            diagnostics.append("  - User Principal Name: ").append(userPrincipalName).append("\n");
            diagnostics.append("  - Base Path: ").append(basePath).append("\n");
            diagnostics.append("  - Mode: OneDrive for Business\n\n");
            
            diagnostics.append("Input:\n");
            diagnostics.append("  - Raw folder path: ").append(folderPath).append("\n\n");
            
            String normalizedPath = normalizeFolderInput(folderPath);
            diagnostics.append("Normalization:\n");
            diagnostics.append("  - Normalized path: ").append(normalizedPath).append("\n\n");
            
            String fullPath = PathUtils.combine(basePath, normalizedPath);
            diagnostics.append("Resolved Path:\n");
            diagnostics.append("  - Full OneDrive path: ").append(fullPath).append("\n\n");
            
            diagnostics.append("Graph API Call:\n");
            String graphUrl = String.format("GET /users/%s/drive/root:/%s:/children", userPrincipalName, fullPath);
            diagnostics.append("  - Endpoint: ").append(graphUrl).append("\n\n");
            
            logger.info("Testing folder access for path: {}", folderPath);
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            diagnostics.append("Authentication: SUCCESS\n");
            diagnostics.append("  - Graph client initialized\n");
            diagnostics.append("  - Token acquired\n\n");
            
            DriveItemCollectionPage items = client
                    .users(userPrincipalName)
                    .drive()
                    .root()
                    .itemWithPath(fullPath)
                    .children()
                    .buildRequest()
                    .get();
            
            diagnostics.append("Folder Access: SUCCESS\n");
            
            int fileCount = 0;
            int folderCount = 0;
            List<String> sampleFiles = new ArrayList<>();
            
            if (items != null && items.getCurrentPage() != null) {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.file != null) {
                        fileCount++;
                        if (sampleFiles.size() < 5) {
                            sampleFiles.add(item.name);
                        }
                    } else if (item.folder != null) {
                        folderCount++;
                    }
                }
            }
            
            diagnostics.append("  - Files found: ").append(fileCount).append("\n");
            diagnostics.append("  - Folders found: ").append(folderCount).append("\n");
            
            if (!sampleFiles.isEmpty()) {
                diagnostics.append("  - Sample files (up to 5):\n");
                for (String fileName : sampleFiles) {
                    diagnostics.append("    * ").append(fileName).append("\n");
                }
            }
            
            diagnostics.append("\n=== TEST PASSED ===\n");
            logger.info("Folder access test PASSED for path: {}", folderPath);
            
            return diagnostics.toString();
            
        } catch (Exception e) {
            diagnostics.append("Folder Access: FAILED\n");
            diagnostics.append("  - Error Type: ").append(e.getClass().getSimpleName()).append("\n");
            diagnostics.append("  - Error Message: ").append(e.getMessage()).append("\n");
            
            if (e.getCause() != null) {
                diagnostics.append("  - Cause: ").append(e.getCause().getMessage()).append("\n");
            }
            
            diagnostics.append("\n=== TEST FAILED ===\n");
            diagnostics.append("\nFull Error Details:\n");
            diagnostics.append(e.toString()).append("\n");
            
            logger.error("Folder access test FAILED for path: {}", folderPath, e);
            
            return diagnostics.toString();
        }
    }
}
