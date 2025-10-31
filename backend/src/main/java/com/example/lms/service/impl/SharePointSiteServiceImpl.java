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
}
