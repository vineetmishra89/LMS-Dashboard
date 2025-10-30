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
            logger.info("Listing files in OneDrive folder: {}", folderPath);
            
            String fullPath = PathUtils.combine(basePath, folderPath);
            logger.debug("Full path: {}", fullPath);
            
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
            
            logger.info("Found {} files in OneDrive folder: {}", fileNames.size(), folderPath);
            return fileNames;
            
        } catch (Exception e) {
            logger.error("Error listing files in OneDrive folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list files in OneDrive folder: " + folderPath + 
                    ". Error: " + e.getMessage(), e);
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
}
