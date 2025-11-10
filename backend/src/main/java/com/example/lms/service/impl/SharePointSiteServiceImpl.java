package com.example.lms.service.impl;

import com.example.lms.service.SharePointService;
import com.example.lms.util.PathUtils;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Site;
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
 * SharePoint service implementation for SharePoint site document libraries.
 * Uses Microsoft Graph API to access SharePoint site drives and folders.
 * 
 * Configuration properties:
 * - graph.mode: Must be set to "site"
 * - graph.tenant-id: Azure AD tenant ID
 * - graph.client-id: Azure AD app client ID
 * - graph.client-secret: Azure AD app client secret
 * - graph.site-hostname: SharePoint site hostname (e.g., "irissoft.sharepoint.com")
 * - graph.site-path: SharePoint site path (e.g., "sites/learning")
 * - graph.drive-id: (Optional) Specific drive ID. If not provided, uses default document library
 * - graph.base-path: Base path within the drive
 * 
 * Graph API endpoint pattern: /sites/{siteId}/drives/{driveId}/root:/{path}:/children
 */
@Service
@ConditionalOnProperty(name = "graph.mode", havingValue = "site")
public class SharePointSiteServiceImpl implements SharePointService {

    private static final Logger logger = LoggerFactory.getLogger(SharePointSiteServiceImpl.class);

    private final GraphClientProvider graphClientProvider;

    @Value("${graph.site-hostname}")
    private String siteHostname;

    @Value("${graph.site-path}")
    private String sitePath;

    @Value("${graph.drive-id:#{null}}")
    private String driveId;

    @Value("${graph.base-path}")
    private String basePath;

    private String cachedSiteId;

    public SharePointSiteServiceImpl(GraphClientProvider graphClientProvider) {
        this.graphClientProvider = graphClientProvider;
        logger.info("SharePointSiteServiceImpl initialized (SharePoint site mode)");
    }

    @Override
    public List<String> listFilesInFolder(String folderPath) {
        try {
            logger.info("Listing files in SharePoint site folder (raw input): {}", folderPath);
            
            String normalizedPath = normalizeFolderInput(folderPath);
            logger.info("Normalized folder path: {}", normalizedPath);
            
            String fullPath = PathUtils.combine(basePath, normalizedPath);
            logger.info("Resolved SharePoint site path: {}", fullPath);
            
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            if (cachedSiteId == null) {
                cachedSiteId = resolveSiteId(client);
                logger.info("Resolved site ID: {}", cachedSiteId);
            }
            
            DriveItemCollectionPage items;
            
            if (driveId != null && !driveId.trim().isEmpty()) {
                logger.debug("Using specific drive ID: {}", driveId);
                items = client
                        .sites(cachedSiteId)
                        .drives(driveId)
                        .root()
                        .itemWithPath(fullPath)
                        .children()
                        .buildRequest()
                        .get();
            } else {
                logger.debug("Using default document library");
                items = client
                        .sites(cachedSiteId)
                        .drive()
                        .root()
                        .itemWithPath(fullPath)
                        .children()
                        .buildRequest()
                        .get();
            }
            
            List<String> fileNames = new ArrayList<>();
            
            if (items != null && items.getCurrentPage() != null) {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.file != null) {
                        fileNames.add(item.name);
                        logger.debug("Found file: {}", item.name);
                    }
                }
            }
            
            logger.info("Found {} files in SharePoint site folder: {}", fileNames.size(), normalizedPath);
            return fileNames;
            
        } catch (Exception e) {
            logger.error("Error listing files in SharePoint site folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list files in SharePoint site folder: " + folderPath + 
                    ". Error: " + e.getMessage(), e);
        }
    }
    
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
                    extractedPath = path.substring(rIndex + 3);
                } else {
                    extractedPath = path.substring(fIndex + 5);
                }
                
                extractedPath = URLDecoder.decode(extractedPath, StandardCharsets.UTF_8);
                logger.debug("Extracted path from /:f:/r/ format: {}", extractedPath);
            }
            else if (uri.getQuery() != null && uri.getQuery().contains("id=")) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        String idValue = param.substring(3);
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
            
            if (extractedPath.startsWith("sites/")) {
                int thirdSlash = extractedPath.indexOf('/', 6);
                if (thirdSlash != -1) {
                    int fourthSlash = extractedPath.indexOf('/', thirdSlash + 1);
                    if (fourthSlash != -1) {
                        extractedPath = extractedPath.substring(fourthSlash + 1);
                        logger.debug("Stripped sites/ prefix, result: {}", extractedPath);
                    }
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

    /**
     * Resolves the SharePoint site ID from hostname and site path.
     * 
     * @param client Graph service client
     * @return Site ID
     * @throws RuntimeException if site resolution fails
     */
    private String resolveSiteId(GraphServiceClient<Request> client) {
        try {
            logger.info("Resolving SharePoint site ID for: {}:/{}", siteHostname, sitePath);
            
            String siteUrl = String.format("%s:/%s", siteHostname, sitePath);
            
            Site site = client
                    .sites(siteUrl)
                    .buildRequest()
                    .get();
            
            if (site == null || site.id == null) {
                throw new RuntimeException("Failed to resolve SharePoint site. Site not found or access denied.");
            }
            
            logger.info("Successfully resolved site ID: {}", site.id);
            return site.id;
            
        } catch (Exception e) {
            logger.error("Error resolving SharePoint site ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve SharePoint site ID for " + siteHostname + ":/" + sitePath + 
                    ". Verify site path and permissions. Error: " + e.getMessage(), e);
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
        
        if (siteHostname == null || siteHostname.trim().isEmpty()) {
            missingProperties.add("graph.site-hostname");
        }
        if (sitePath == null || sitePath.trim().isEmpty()) {
            missingProperties.add("graph.site-path");
        }
        if (basePath == null || basePath.trim().isEmpty()) {
            missingProperties.add("graph.base-path");
        }
        
        if (!missingProperties.isEmpty()) {
            String message = "Missing required SharePoint site configuration properties: " + 
                    String.join(", ", missingProperties);
            logger.error(message);
            throw new IllegalStateException(message);
        }
        
        logger.info("SharePoint site configuration validated successfully");
    }
    
    @Override
    public String testFolderAccess(String folderPath) {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== SharePoint Site Folder Access Test ===\n\n");
        
        try {
            diagnostics.append("Configuration:\n");
            diagnostics.append("  - Site Hostname: ").append(siteHostname).append("\n");
            diagnostics.append("  - Site Path: ").append(sitePath).append("\n");
            diagnostics.append("  - Drive ID: ").append(driveId != null ? driveId : "(default)").append("\n");
            diagnostics.append("  - Base Path: ").append(basePath).append("\n");
            diagnostics.append("  - Mode: SharePoint Site\n\n");
            
            diagnostics.append("Input:\n");
            diagnostics.append("  - Raw folder path: ").append(folderPath).append("\n\n");
            
            String normalizedPath = normalizeFolderInput(folderPath);
            diagnostics.append("Normalization:\n");
            diagnostics.append("  - Normalized path: ").append(normalizedPath).append("\n\n");
            
            String fullPath = PathUtils.combine(basePath, normalizedPath);
            diagnostics.append("Resolved Path:\n");
            diagnostics.append("  - Full SharePoint path: ").append(fullPath).append("\n\n");
            
            logger.info("Testing folder access for path: {}", folderPath);
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            diagnostics.append("Authentication: SUCCESS\n");
            diagnostics.append("  - Graph client initialized\n");
            diagnostics.append("  - Token acquired\n\n");
            
            if (cachedSiteId == null) {
                cachedSiteId = resolveSiteId(client);
            }
            
            diagnostics.append("Site Resolution: SUCCESS\n");
            diagnostics.append("  - Site ID: ").append(cachedSiteId).append("\n\n");
            
            String endpoint = driveId != null && !driveId.trim().isEmpty()
                ? String.format("GET /sites/%s/drives/%s/root:/%s:/children", cachedSiteId, driveId, fullPath)
                : String.format("GET /sites/%s/drive/root:/%s:/children", cachedSiteId, fullPath);
            
            diagnostics.append("Graph API Call:\n");
            diagnostics.append("  - Endpoint: ").append(endpoint).append("\n\n");
            
            DriveItemCollectionPage items;
            
            if (driveId != null && !driveId.trim().isEmpty()) {
                items = client
                        .sites(cachedSiteId)
                        .drives(driveId)
                        .root()
                        .itemWithPath(fullPath)
                        .children()
                        .buildRequest()
                        .get();
            } else {
                items = client
                        .sites(cachedSiteId)
                        .drive()
                        .root()
                        .itemWithPath(fullPath)
                        .children()
                        .buildRequest()
                        .get();
            }
            
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
    
    @Override
    public String listSiteDrives() {
        StringBuilder result = new StringBuilder();
        result.append("=== SharePoint Site Drives (Document Libraries) ===\n\n");
        
        try {
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            if (cachedSiteId == null) {
                cachedSiteId = resolveSiteId(client);
            }
            
            result.append("Site Information:\n");
            result.append("  - Site Hostname: ").append(siteHostname).append("\n");
            result.append("  - Site Path: ").append(sitePath).append("\n");
            result.append("  - Site ID: ").append(cachedSiteId).append("\n\n");
            
            logger.info("Listing drives for SharePoint site: {}", cachedSiteId);
            
            var drives = client
                    .sites(cachedSiteId)
                    .drives()
                    .buildRequest()
                    .select("id,name,webUrl,driveType")
                    .get();
            
            if (drives == null || drives.getCurrentPage() == null || drives.getCurrentPage().isEmpty()) {
                result.append("No drives found on this site.\n");
                logger.warn("No drives found for site: {}", cachedSiteId);
                return result.toString();
            }
            
            result.append("Document Libraries (Drives):\n");
            result.append("Total: ").append(drives.getCurrentPage().size()).append("\n\n");
            
            int index = 1;
            for (var drive : drives.getCurrentPage()) {
                result.append(index++).append(". ").append(drive.name).append("\n");
                result.append("   - Drive ID: ").append(drive.id).append("\n");
                result.append("   - Drive Type: ").append(drive.driveType != null ? drive.driveType : "N/A").append("\n");
                result.append("   - Web URL: ").append(drive.webUrl != null ? drive.webUrl : "N/A").append("\n");
                
                if (drive.name != null && drive.name.equals("Documents")) {
                    result.append("   - Note: This is the DEFAULT document library (\"Shared Documents\")\n");
                    result.append("   - To use this library, leave graph.drive-id empty in configuration\n");
                }
                result.append("\n");
            }
            
            result.append("Configuration Tips:\n");
            result.append("  - To use the default library (usually \"Documents\"), leave graph.drive-id empty\n");
            result.append("  - To use a specific library, set graph.drive-id to the Drive ID shown above\n");
            result.append("  - Do NOT include the library name in graph.base-path\n");
            result.append("  - Set graph.base-path to a folder path within the library (or leave empty for root)\n");
            
            logger.info("Successfully listed {} drives for site: {}", drives.getCurrentPage().size(), cachedSiteId);
            return result.toString();
            
        } catch (Exception e) {
            result.append("ERROR: Failed to list drives\n");
            result.append("  - Error Type: ").append(e.getClass().getSimpleName()).append("\n");
            result.append("  - Error Message: ").append(e.getMessage()).append("\n");
            
            if (e.getCause() != null) {
                result.append("  - Cause: ").append(e.getCause().getMessage()).append("\n");
            }
            
            logger.error("Error listing drives for site: {}", cachedSiteId, e);
            return result.toString();
        }
    }
}
