package com.example.lms.service.impl;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.FileNode;
import com.example.lms.dto.FolderNode;
import com.example.lms.dto.SharePointSyncResult;
import com.example.lms.repo.CourseDetailRepository;
import com.example.lms.repo.CourseRepository;
import com.example.lms.service.UserTokenSharePointService;
import com.example.lms.util.JwtClaimExtractor;
import com.example.lms.util.SessionSequenceParser;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Implementation of UserTokenSharePointService for accessing OneDrive/SharePoint
 * using user-provided bearer tokens (delegated permissions).
 */
@Service
public class UserTokenSharePointServiceImpl implements UserTokenSharePointService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserTokenSharePointServiceImpl.class);
    
    private final GraphClientProvider graphClientProvider;
    private final CourseRepository courseRepository;
    private final CourseDetailRepository courseDetailRepository;
    
    @Value("${graph.user.drive-id:}")
    private String driveId;
    
    @Value("${graph.user.root-folder-id:}")
    private String rootFolderId;
    
    public UserTokenSharePointServiceImpl(GraphClientProvider graphClientProvider,
                                          CourseRepository courseRepository,
                                          CourseDetailRepository courseDetailRepository) {
        this.graphClientProvider = graphClientProvider;
        this.courseRepository = courseRepository;
        this.courseDetailRepository = courseDetailRepository;
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
    
    @Override
    public FolderNode listFoldersAndFilesRecursivelyFromIds(String bearerToken) {
        try {
            validateConfiguration();
            
            logger.info("Listing folders and files recursively using drive ID: {} and folder ID: {}", driveId, rootFolderId);
            
            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);
            
            DriveItem rootItem = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(rootFolderId)
                    .buildRequest()
                    .get();
            
            if (rootItem == null) {
                throw new RuntimeException("Root folder not found with ID: " + rootFolderId);
            }
            
            FolderNode rootFolder = new FolderNode();
            rootFolder.setName(rootItem.name);
            rootFolder.setPath(rootItem.name);
            rootFolder.setWebUrl(rootItem.webUrl);
            
            listFolderContentsByIdRecursively(client, driveId, rootFolderId, rootFolder);
            
            logger.info("Successfully listed folders and files recursively from IDs");
            return rootFolder;
            
        } catch (Exception e) {
            logger.error("Error listing folders and files from IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates that drive ID and folder ID are configured.
     */
    private void validateConfiguration() {
        if (driveId == null || driveId.trim().isEmpty() || driveId.equals("your-drive-id-here")) {
            throw new IllegalStateException("graph.user.drive-id is not configured in application.properties");
        }
        if (rootFolderId == null || rootFolderId.trim().isEmpty() || rootFolderId.equals("your-root-folder-id-here")) {
            throw new IllegalStateException("graph.user.root-folder-id is not configured in application.properties");
        }
    }
    
    /**
     * Recursively lists all folders and files using drive ID and item ID.
     */
    private void listFolderContentsByIdRecursively(GraphServiceClient<Request> client, String driveId, String folderId, FolderNode folderNode) {
        try {
            logger.debug("Listing contents of folder ID: {}", folderId);
            
            DriveItemCollectionPage items = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(folderId)
                    .children()
                    .buildRequest()
                    .get();
            
            if (items == null || items.getCurrentPage() == null) {
                logger.debug("No items found in folder ID: {}", folderId);
                return;
            }
            
            processItemsById(client, driveId, folderNode, items);
            
            while (items.getNextPage() != null) {
                items = items.getNextPage().buildRequest().get();
                if (items != null && items.getCurrentPage() != null) {
                    processItemsById(client, driveId, folderNode, items);
                }
            }
            
            logger.debug("Finished listing contents of folder ID: {} (Files: {}, Folders: {})", 
                    folderId, folderNode.getFiles().size(), folderNode.getFolders().size());
            
        } catch (Exception e) {
            logger.error("Error listing contents of folder ID '{}': {}", folderId, e.getMessage(), e);
            throw new RuntimeException("Failed to list folder contents: " + folderId, e);
        }
    }
    
    /**
     * Processes items from a DriveItemCollectionPage using IDs for recursion.
     */
    private void processItemsById(GraphServiceClient<Request> client, String driveId, FolderNode parentNode, DriveItemCollectionPage items) {
        for (DriveItem item : items.getCurrentPage()) {
            if (item.folder != null) {
                FolderNode childFolder = new FolderNode();
                childFolder.setName(item.name);
                childFolder.setPath(parentNode.getPath() + "/" + item.name);
                childFolder.setWebUrl(item.webUrl);
                
                parentNode.addFolder(childFolder);
                
                listFolderContentsByIdRecursively(client, driveId, item.id, childFolder);
                
            } else if (item.file != null) {
                FileNode fileNode = new FileNode();
                fileNode.setName(item.name);
                fileNode.setPath(parentNode.getPath() + "/" + item.name);
                fileNode.setWebUrl(item.webUrl);
                fileNode.setSize(item.size);
                fileNode.setLastModified(item.lastModifiedDateTime);
                
                parentNode.addFile(fileNode);
            }
        }
    }
    
    @Override
    @Transactional
    public SharePointSyncResult syncModulesFromSharePoint(String bearerToken, boolean dryRun) {
        SharePointSyncResult result = SharePointSyncResult.builder()
                .dryRun(dryRun)
                .build();
        
        try {
            validateConfiguration();
            
            logger.info("Starting SharePoint sync (dryRun={})", dryRun);
            
            String username = JwtClaimExtractor.extractUsername(bearerToken);
            if (username == null || username.trim().isEmpty()) {
                result.addError("Failed to extract username from bearer token");
                return result;
            }
            logger.info("Extracted username from token: {}", username);
            
            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);
            
            DriveItem rootItem = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(rootFolderId)
                    .buildRequest()
                    .get();
            
            if (rootItem == null) {
                result.addError("Root folder not found with ID: " + rootFolderId);
                return result;
            }
            
            java.util.Set<String> visitedFolders = new java.util.HashSet<>();
            processFolderForSync(client, driveId, rootFolderId, rootItem.name, rootItem.webUrl, username, result, dryRun, visitedFolders);
            
            logger.info("SharePoint sync completed: foldersProcessed={}, coursesMatched={}, coursesUpdated={}, modulesInserted={}, modulesUpdated={}, modulesSkipped={}, errors={}",
                    result.getFoldersProcessed(), result.getCoursesMatched(), result.getCoursesUpdated(),
                    result.getModulesInserted(), result.getModulesUpdated(), result.getModulesSkipped(), result.getErrors().size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error during SharePoint sync: {}", e.getMessage(), e);
            result.addError("Sync failed: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Processes a folder and its children for sync operation.
     */
    private void processFolderForSync(GraphServiceClient<Request> client, String driveId, String folderId,
                                      String folderName, String folderWebUrl, String username,
                                      SharePointSyncResult result, boolean dryRun, java.util.Set<String> visitedFolders) {
        try {
            String folderKey = driveId + ":" + folderId;
            if (visitedFolders.contains(folderKey)) {
                logger.debug("Skipping already visited folder: {} ({})", folderName, folderKey);
                return;
            }
            visitedFolders.add(folderKey);
            
            result.setFoldersProcessed(result.getFoldersProcessed() + 1);
            logger.info("Processing folder: {} (driveId={}, folderId={}, url={})", folderName, driveId, folderId, folderWebUrl);
            
            Optional<CourseSummary> courseOpt = matchCourseByFolderPath(folderName, folderWebUrl);
            CourseSummary matchedCourse = null;
            
            if (courseOpt.isPresent()) {
                matchedCourse = courseOpt.get();
                result.setCoursesMatched(result.getCoursesMatched() + 1);
                logger.info("Matched folder '{}' to training: {} (ID: {})", folderName, matchedCourse.getTopics(), matchedCourse.getTrainingId());
                
                if (matchedCourse.getFolder_path() == null || !matchedCourse.getFolder_path().equals(folderWebUrl)) {
                    if (!dryRun) {
                        matchedCourse.setFolder_path(folderWebUrl);
                        matchedCourse.setUpdatedBy(username);
                        matchedCourse.setUpdatedTs(OffsetDateTime.now());
                        courseRepository.save(matchedCourse);
                    }
                    result.setCoursesUpdated(result.getCoursesUpdated() + 1);
                    logger.info("Updated folder_path for training ID {}: {}", matchedCourse.getTrainingId(), folderWebUrl);
                }
            } else {
                logger.debug("No course matched for folder: {}", folderName);
            }
            
            DriveItemCollectionPage items = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(folderId)
                    .children()
                    .buildRequest()
                    .get();
            
            if (items == null || items.getCurrentPage() == null) {
                logger.debug("No children found in folder: {}", folderName);
                return;
            }
            
            do {
                int childCount = items.getCurrentPage().size();
                logger.debug("Processing {} children in folder: {}", childCount, folderName);
                
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.folder != null) {
                        logger.debug("Found subfolder: {} (id={})", item.name, item.id);
                        processFolderForSync(client, driveId, item.id, item.name, item.webUrl, username, result, dryRun, visitedFolders);
                        
                    } else if (item.remoteItem != null && item.remoteItem.folder != null) {
                        String remoteDriveId = item.remoteItem.parentReference != null && item.remoteItem.parentReference.driveId != null
                                ? item.remoteItem.parentReference.driveId
                                : driveId;
                        String remoteItemId = item.remoteItem.id;
                        String remoteWebUrl = item.remoteItem.webUrl != null ? item.remoteItem.webUrl : item.webUrl;
                        
                        logger.info("Found remote folder (shortcut): {} (remoteDriveId={}, remoteItemId={})", item.name, remoteDriveId, remoteItemId);
                        processFolderForSync(client, remoteDriveId, remoteItemId, item.name, remoteWebUrl, username, result, dryRun, visitedFolders);
                        
                    } else if (item.file != null && item.name != null && item.name.toLowerCase().endsWith(".mp4")) {
                        if (matchedCourse != null) {
                            logger.debug("Found .mp4 file: {} in matched course folder", item.name);
                            processVideoFile(item, matchedCourse, username, result, dryRun);
                        } else {
                            logger.debug("Found .mp4 file: {} but no course matched for this folder", item.name);
                        }
                    }
                }
                
                if (items.getNextPage() != null) {
                    logger.debug("Fetching next page of children for folder: {}", folderName);
                    items = items.getNextPage().buildRequest().get();
                } else {
                    break;
                }
            } while (items != null && items.getCurrentPage() != null);
            
        } catch (Exception e) {
            logger.error("Error processing folder '{}': {}", folderName, e.getMessage(), e);
            result.addError("Error processing folder '" + folderName + "': " + e.getMessage());
        }
    }
    
    /**
     * Matches CourseSummary by folder_path (supports both name and URL matching).
     */
    private Optional<CourseSummary> matchCourseByFolderPath(String folderName, String folderWebUrl) {
        Optional<CourseSummary> courseOpt = courseRepository.findByFolderPath(folderWebUrl);
        if (courseOpt.isPresent()) {
            logger.debug("Matched by URL: {}", folderWebUrl);
            return courseOpt;
        }
        
        courseOpt = courseRepository.findByFolderPathIgnoreCase(folderName);
        if (courseOpt.isPresent()) {
            logger.debug("Matched by name: {}", folderName);
            return courseOpt;
        }
        
        return Optional.empty();
    }
    
    /**
     * Processes .mp4 files in a folder and inserts/updates course modules.
     */
    
    /**
     * Processes a single video file and inserts/updates course module.
     */
    private void processVideoFile(DriveItem item, CourseSummary course, String username,
                                   SharePointSyncResult result, boolean dryRun) {
        try {
            String filename = item.name;
            String webUrl = item.webUrl;
            
            String moduleName = SessionSequenceParser.removeExtension(filename);
            
            Integer seqId = SessionSequenceParser.extractSessionNumber(filename);
            
            logger.debug("Processing file: {} -> moduleName={}, seqId={}, webUrl={}", filename, moduleName, seqId, webUrl);
            
            Optional<CourseDetail> existingModuleOpt = courseDetailRepository
                    .findByCourseTrainingIdAndTrainingLink(course.getTrainingId(), webUrl);
            
            if (existingModuleOpt.isPresent()) {
                CourseDetail existingModule = existingModuleOpt.get();
                boolean updated = false;
                
                if (!moduleName.equals(existingModule.getSummary())) {
                    existingModule.setSummary(moduleName);
                    updated = true;
                }
                
                if (!moduleName.equals(existingModule.getDetails())) {
                    existingModule.setDetails(moduleName);
                    updated = true;
                }
                
                if ((seqId == null && existingModule.getSeqId() != null) ||
                    (seqId != null && !seqId.equals(existingModule.getSeqId()))) {
                    existingModule.setSeqId(seqId);
                    updated = true;
                }
                
                if (updated) {
                    if (!dryRun) {
                        existingModule.setUpdatedBy(username);
                        existingModule.setUpdatedTs(OffsetDateTime.now());
                        courseDetailRepository.save(existingModule);
                    }
                    result.setModulesUpdated(result.getModulesUpdated() + 1);
                    logger.info("Updated module: {} (ID: {})", moduleName, existingModule.getModuleId());
                } else {
                    result.setModulesSkipped(result.getModulesSkipped() + 1);
                    logger.debug("Module unchanged, skipped: {}", moduleName);
                }
                
            } else {
                if (!dryRun) {
                    CourseDetail newModule = CourseDetail.builder()
                            .summary(moduleName)
                            .details(moduleName)
                            .trainingLink(webUrl)
                            .seqId(seqId)
                            .course(course)
                            .createdBy(username)
                            .createdTs(OffsetDateTime.now())
                            .updatedBy(username)
                            .updatedTs(OffsetDateTime.now())
                            .build();
                    
                    courseDetailRepository.save(newModule);
                    logger.info("Inserted new module: {} (seqId: {}) for training ID {}", moduleName, seqId, course.getTrainingId());
                }
                result.setModulesInserted(result.getModulesInserted() + 1);
            }
            
        } catch (Exception e) {
            logger.error("Error processing video file '{}': {}", item.name, e.getMessage(), e);
            result.addError("Error processing file '" + item.name + "': " + e.getMessage());
        }
    }
}
