package com.example.lms.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for interacting with Microsoft SharePoint/OneDrive using Microsoft Graph API.
 * Provides methods to read folder contents from SharePoint Online or OneDrive for Business.
 * 
 * Configuration properties:
 * - graph.tenant-id: Azure AD tenant ID
 * - graph.client-id: Azure AD app client ID
 * - graph.client-secret: Azure AD app client secret
 * - graph.user-principal-name: User's email for OneDrive access
 * - graph.base-path: Base path within OneDrive/SharePoint
 */
@Service
public class SharePointService {

    private static final Logger logger = LoggerFactory.getLogger(SharePointService.class);
    private static final List<String> GRAPH_SCOPES = List.of("https://graph.microsoft.com/.default");

    @Value("${graph.tenant-id}")
    private String tenantId;

    @Value("${graph.client-id}")
    private String clientId;

    @Value("${graph.client-secret}")
    private String clientSecret;

    @Value("${graph.user-principal-name}")
    private String userPrincipalName;

    @Value("${graph.base-path}")
    private String basePath;

    private GraphServiceClient<Request> graphClient;

    /**
     * Initializes the Microsoft Graph client with client credentials authentication.
     * This method is called lazily on first use.
     * 
     * @return Configured GraphServiceClient instance
     */
    private GraphServiceClient<Request> getGraphClient() {
        if (graphClient == null) {
            try {
                logger.info("Initializing Microsoft Graph client for tenant: {}", tenantId);
                
                ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId)
                        .build();

                TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                        GRAPH_SCOPES, credential);

                graphClient = GraphServiceClient.builder()
                        .authenticationProvider(authProvider)
                        .buildClient();
                
                logger.info("Microsoft Graph client initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Microsoft Graph client: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize Microsoft Graph client", e);
            }
        }
        return graphClient;
    }

    /**
     * Lists all files in a SharePoint/OneDrive folder.
     * 
     * @param folderPath Relative path to the folder (e.g., "Training Materials/2024")
     * @return List of file names with extensions
     * @throws RuntimeException if folder access fails
     */
    public List<String> listFilesInFolder(String folderPath) {
        try {
            logger.info("Listing files in folder: {}", folderPath);
            
            String fullPath = constructFullPath(folderPath);
            logger.debug("Full path: {}", fullPath);
            
            GraphServiceClient<Request> client = getGraphClient();
            
            String itemPath = String.format("/users/%s/drive/root:/%s:/children", 
                    userPrincipalName, fullPath);
            
            logger.debug("Requesting items from path: {}", itemPath);
            
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
            
            logger.info("Found {} files in folder: {}", fileNames.size(), folderPath);
            return fileNames;
            
        } catch (Exception e) {
            logger.error("Error listing files in folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list files in folder: " + folderPath, e);
        }
    }

    /**
     * Constructs the full path by combining base path and folder path.
     * Handles path separators and ensures proper formatting.
     * 
     * @param folderPath Relative folder path
     * @return Full path combining base path and folder path
     */
    private String constructFullPath(String folderPath) {
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
     * Validates that all required configuration properties are set.
     * 
     * @throws IllegalStateException if any required property is missing
     */
    public void validateConfiguration() {
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
        
        logger.info("SharePoint configuration validated successfully");
    }
}
