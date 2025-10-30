package com.example.lms.service;

import java.util.List;

/**
 * Service interface for interacting with Microsoft SharePoint/OneDrive using Microsoft Graph API.
 * Provides methods to read folder contents from SharePoint Online or OneDrive for Business.
 * 
 * Implementations:
 * - OneDriveSharePointServiceImpl: For OneDrive for Business (personal folders)
 * - SharePointSiteServiceImpl: For SharePoint site document libraries
 * 
 * Configuration:
 * - Set graph.mode=onedrive (default) or graph.mode=site to select implementation
 * - Common properties: graph.tenant-id, graph.client-id, graph.client-secret
 * - OneDrive-specific: graph.user-principal-name, graph.base-path
 * - SharePoint site-specific: graph.site-hostname, graph.site-path, graph.drive-id (optional)
 */
public interface SharePointService {

    /**
     * Lists all files in a SharePoint/OneDrive folder.
     * 
     * @param folderPath Relative path to the folder (e.g., "Training Materials/2024")
     * @return List of file names with extensions
     * @throws RuntimeException if folder access fails
     */
    List<String> listFilesInFolder(String folderPath);

    /**
     * Validates that all required configuration properties are set.
     * 
     * @throws IllegalStateException if any required property is missing
     */
    void validateConfiguration();
}
