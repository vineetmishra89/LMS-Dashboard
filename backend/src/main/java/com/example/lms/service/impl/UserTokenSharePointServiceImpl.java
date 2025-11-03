package com.example.lms.service.impl;

import com.example.lms.dto.FileNode;
import com.example.lms.dto.FolderNode;
import com.example.lms.service.UserTokenSharePointService;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of UserTokenSharePointService for accessing OneDrive/SharePoint
 * using user-provided bearer tokens (delegated permissions).
 */
@Service
public class UserTokenSharePointServiceImpl implements UserTokenSharePointService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserTokenSharePointServiceImpl.class);
    
    private final GraphClientProvider graphClientProvider;
    
    public UserTokenSharePointServiceImpl(GraphClientProvider graphClientProvider) {
        this.graphClientProvider = graphClientProvider;
    }
    
    @Override
    public FolderNode listFoldersAndFilesRecursively(String bearerToken, String folderUrl) {
        try {
            logger.info("Listing folders and files recursively from URL: {}", folderUrl);
            
            String relativePath = parseFolderUrl(folderUrl);
            logger.info("Parsed relative path: {}", relativePath);
            
            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);
            
            FolderNode rootFolder = new FolderNode();
            rootFolder.setName(getLastSegment(relativePath));
            rootFolder.setPath(relativePath);
            
            listFolderContentsRecursively(client, relativePath, rootFolder);
            
            logger.info("Successfully listed folders and files recursively");
            return rootFolder;
            
        } catch (Exception e) {
            logger.error("Error listing folders and files recursively: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recursively lists all folders and files in a folder.
     */
    private void listFolderContentsRecursively(GraphServiceClient<Request> client, String folderPath, FolderNode folderNode) {
        try {
            logger.debug("Listing contents of folder: {}", folderPath);
            
            DriveItemCollectionPage items = client
                    .me()
                    .drive()
                    .root()
                    .itemWithPath(folderPath)
                    .children()
                    .buildRequest()
                    .get();
            
            if (items == null || items.getCurrentPage() == null) {
                logger.debug("No items found in folder: {}", folderPath);
                return;
            }
            
            processItems(client, folderPath, folderNode, items);
            
            while (items.getNextPage() != null) {
                items = items.getNextPage().buildRequest().get();
                if (items != null && items.getCurrentPage() != null) {
                    processItems(client, folderPath, folderNode, items);
                }
            }
            
            logger.debug("Finished listing contents of folder: {} (Files: {}, Folders: {})", 
                    folderPath, folderNode.getFiles().size(), folderNode.getFolders().size());
            
        } catch (Exception e) {
            logger.error("Error listing contents of folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list folder contents: " + folderPath, e);
        }
    }
    
    /**
     * Processes items from a DriveItemCollectionPage.
     */
    private void processItems(GraphServiceClient<Request> client, String parentPath, FolderNode parentNode, DriveItemCollectionPage items) {
        for (DriveItem item : items.getCurrentPage()) {
            String itemPath = parentPath + "/" + item.name;
            
            if (item.folder != null) {
                FolderNode childFolder = new FolderNode();
                childFolder.setName(item.name);
                childFolder.setPath(itemPath);
                childFolder.setWebUrl(item.webUrl);
                
                parentNode.addFolder(childFolder);
                
                listFolderContentsRecursively(client, itemPath, childFolder);
                
            } else if (item.file != null) {
                FileNode fileNode = new FileNode();
                fileNode.setName(item.name);
                fileNode.setPath(itemPath);
                fileNode.setWebUrl(item.webUrl);
                fileNode.setSize(item.size);
                fileNode.setLastModified(item.lastModifiedDateTime);
                
                parentNode.addFile(fileNode);
            }
        }
    }
    
    /**
     * Parses a SharePoint/OneDrive folder URL to extract the relative path.
     * Handles onedrive.aspx?id=... format and other SharePoint URL formats.
     */
    private String parseFolderUrl(String folderUrl) {
        try {
            URI uri = new URI(folderUrl);
            String extractedPath = null;
            
            if (uri.getQuery() != null && uri.getQuery().contains("id=")) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        String idValue = param.substring(3); // Skip "id="
                        extractedPath = URLDecoder.decode(idValue, StandardCharsets.UTF_8);
                        
                        if (extractedPath.startsWith("/")) {
                            extractedPath = extractedPath.substring(1);
                        }
                        
                        logger.debug("Extracted path from id parameter: {}", extractedPath);
                        break;
                    }
                }
            } else {
                extractedPath = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
                if (extractedPath.startsWith("/")) {
                    extractedPath = extractedPath.substring(1);
                }
                logger.debug("Extracted path from URI path: {}", extractedPath);
            }
            
            if (extractedPath == null || extractedPath.isEmpty()) {
                throw new IllegalArgumentException("Could not extract folder path from URL");
            }
            
            if (extractedPath.startsWith("personal/")) {
                int secondSlash = extractedPath.indexOf('/', 9); // Find slash after "personal/"
                if (secondSlash != -1) {
                    int thirdSlash = extractedPath.indexOf('/', secondSlash + 1); // Find slash after alias
                    if (thirdSlash != -1) {
                        extractedPath = extractedPath.substring(thirdSlash + 1);
                        logger.debug("Stripped personal/alias prefix, result: {}", extractedPath);
                    }
                }
            }
            
            return extractedPath;
            
        } catch (Exception e) {
            logger.error("Error parsing folder URL: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid folder URL: " + folderUrl, e);
        }
    }
    
    /**
     * Gets the last segment of a path (the folder name).
     */
    private String getLastSegment(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }
}
