package com.example.lms.util;

/**
 * Utility class for path manipulation operations.
 * Provides consistent path handling across SharePoint service implementations.
 */
public class PathUtils {

    private PathUtils() {
    }

    /**
     * Combines base path and folder path into a single path.
     * Handles path separators and ensures proper formatting.
     * 
     * @param basePath Base path (e.g., "Documents/Recordings_All/Training Recordings")
     * @param folderPath Relative folder path (e.g., "2024/January")
     * @return Combined path with proper separators
     * 
     * Examples:
     * - combine("Documents/Training", "2024") -> "Documents/Training/2024"
     * - combine("Documents/Training", "") -> "Documents/Training"
     * - combine("Documents/Training/", "/2024/") -> "Documents/Training/2024"
     */
    public static String combine(String basePath, String folderPath) {
        if (basePath == null) {
            basePath = "";
        }
        if (folderPath == null) {
            folderPath = "";
        }
        
        String cleanFolderPath = folderPath.trim().replaceAll("^/+|/+$", "");
        
        String cleanBasePath = basePath.trim().replaceAll("/+$", "");
        
        if (cleanFolderPath.isEmpty()) {
            return cleanBasePath;
        }
        
        if (cleanBasePath.isEmpty()) {
            return cleanFolderPath;
        }
        
        return cleanBasePath + "/" + cleanFolderPath;
    }
}
