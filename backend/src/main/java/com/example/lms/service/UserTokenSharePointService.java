package com.example.lms.service;

import com.example.lms.dto.FolderNode;
import com.example.lms.dto.SharePointSyncResult;

import java.util.List;

/**
 * Service for accessing SharePoint/OneDrive using user-provided bearer tokens.
 * This service uses delegated permissions (user context) rather than application permissions.
 */
public interface UserTokenSharePointService {
    
    /**
     * Lists all folders and files recursively from a SharePoint/OneDrive folder URL.
     * Uses the provided bearer token for authentication (delegated user permissions).
     * 
     * @param bearerToken User's access token (from Authorization header, without "Bearer " prefix)
     * @param folderUrl SharePoint/OneDrive folder URL (e.g., onedrive.aspx?id=... format)
     * @return FolderNode tree structure containing all folders and files
     * @throws RuntimeException if folder access fails or token is invalid
     */
    FolderNode listFoldersAndFilesRecursively(String bearerToken, String folderUrl);
    
    /**
     * Lists all folders and files recursively using configured drive ID and folder ID.
     * Uses direct Graph API access with drive and item IDs from application.properties.
     * 
     * @param bearerToken User's access token (from Authorization header, without "Bearer " prefix)
     * @return FolderNode tree structure containing all folders and files
     * @throws RuntimeException if folder access fails or token is invalid
     */
    FolderNode listFoldersAndFilesRecursivelyFromIds(String bearerToken);
    
    /**
     * Syncs SharePoint folders and files to LMS database.
     * Matches folders by name or URL, processes .mp4 files, and inserts/updates course modules.
     * 
     * @param bearerToken User's access token (from Authorization header, without "Bearer " prefix)
     * @param dryRun If true, performs validation without database changes
     * @return Sync operation results with counts and errors
     * @throws RuntimeException if sync operation fails
     */
    SharePointSyncResult syncModulesFromSharePoint(String bearerToken, boolean dryRun);
    
    /**
     * Lists all folders and files recursively from a SharePoint/OneDrive folder using folderPath.
     * Uses the provided bearer token for authentication and resolves the folderPath to fetch files.
     * 
     * @param bearerToken User's access token (from Authorization header, without "Bearer " prefix)
     * @param folderPath Folder path from lms_trng_summary table
     * @return FolderNode tree structure containing all folders and files
     * @throws RuntimeException if folder access fails or token is invalid
     */
    FolderNode listFoldersAndFilesRecursivelyByPath(String bearerToken, String folderPath);
    
    /**
     * Fetches all file paths from a SharePoint/OneDrive folder using webUrl.
     * Uses the provided bearer token for authentication.
     * 
     * @param bearerToken User's access token (from Authorization header, without "Bearer " prefix)
     * @param webUrl SharePoint/OneDrive folder webUrl from lms_trng_summary.folder_path column
     * @return List of file paths (webUrls) for all files in the folder and subfolders
     * @throws RuntimeException if folder access fails or token is invalid
     */
    List<String> fetchAllFilePathsFromWebUrl(String bearerToken, String webUrl);
}
