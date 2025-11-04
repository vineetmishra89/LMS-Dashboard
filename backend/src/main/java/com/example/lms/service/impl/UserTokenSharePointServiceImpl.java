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

import java.time.OffsetDateTime;
import java.util.Optional;

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

    @Value("${graph.user.user-principal-name:}")
    private String userPrincipalName;

    @Value("${graph.user.base-path:}")
    private String basePath;

    public UserTokenSharePointServiceImpl(GraphClientProvider graphClientProvider,
                                          CourseRepository courseRepository,
                                          CourseDetailRepository courseDetailRepository) {
        this.graphClientProvider = graphClientProvider;
        this.courseRepository = courseRepository;
        this.courseDetailRepository = courseDetailRepository;
    }

    @Override
    public FolderNode listFoldersAndFilesRecursively(String bearerToken, String folderPath) {
        try {
            logger.info("Listing folders and files recursively for path: {}", folderPath);

            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);

            String[] parsedPath = parseFolderUrl(folderPath);
            String siteDomain = parsedPath[0];
            String sitePath = parsedPath[1];
            String itemPath = parsedPath[2];

            logger.info("Parsed - Domain: {}, Site: {}, Item: {}", siteDomain, sitePath, itemPath);

            FolderNode rootNode = new FolderNode(getLastSegment(itemPath), folderPath);
            listFolderContentsRecursively(client, siteDomain, sitePath, itemPath, rootNode);

            return rootNode;

        } catch (Exception e) {
            logger.error("Error listing folders and files: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files", e);
        }
    }

    private void listFolderContentsRecursively(GraphServiceClient<Request> client, String siteDomain,
                                               String sitePath, String itemPath, FolderNode folderNode) {
        try {
            logger.debug("Listing contents of: {}", itemPath);

            DriveItemCollectionPage items = client
                    .sites(siteDomain, sitePath)
                    .drive()
                    .root()
                    .itemWithPath(itemPath)
                    .children()
                    .buildRequest()
                    .get();

            if (items == null || items.getCurrentPage() == null) {
                logger.debug("No items found in: {}", itemPath);
                return;
            }

            processItems(client, siteDomain, sitePath, itemPath, folderNode, items);

            while (items.getNextPage() != null) {
                items = items.getNextPage().buildRequest().get();
                if (items != null && items.getCurrentPage() != null) {
                    processItems(client, siteDomain, sitePath, itemPath, folderNode, items);
                }
            }

        } catch (Exception e) {
            logger.error("Error listing contents of '{}': {}", itemPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list folder contents: " + itemPath, e);
        }
    }

    private void processItems(GraphServiceClient<Request> client, String siteDomain, String sitePath,
                              String parentPath, FolderNode parentNode, DriveItemCollectionPage items) {
        for (DriveItem item : items.getCurrentPage()) {
            if (item.folder != null) {
                String childPath = parentPath + "/" + item.name;
                FolderNode childNode = new FolderNode(item.name, item.webUrl);
                parentNode.addSubfolder(childNode);
                listFolderContentsRecursively(client, siteDomain, sitePath, childPath, childNode);
            } else if (item.file != null) {
                FileNode fileNode = new FileNode(item.name, item.webUrl);
                parentNode.addFile(fileNode);
            }
        }
    }

    private String[] parseFolderUrl(String folderUrl) {
        try {
            String cleanUrl = folderUrl;
            if (cleanUrl.contains("?")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));
            }

            if (!cleanUrl.contains(".sharepoint.com")) {
                throw new IllegalArgumentException("Invalid SharePoint URL");
            }

            String afterProtocol = cleanUrl.substring(cleanUrl.indexOf("://") + 3);
            String domain = afterProtocol.substring(0, afterProtocol.indexOf("/"));

            String remaining = afterProtocol.substring(afterProtocol.indexOf("/"));

            String sitePath = "";
            String itemPath = "";

            if (remaining.contains("/sites/")) {
                int sitesIndex = remaining.indexOf("/sites/");
                int nextSlash = remaining.indexOf("/", sitesIndex + 7);
                if (nextSlash != -1) {
                    sitePath = remaining.substring(sitesIndex, nextSlash);
                    itemPath = remaining.substring(nextSlash + 1);
                } else {
                    sitePath = remaining.substring(sitesIndex);
                }
            } else if (remaining.contains("/personal/")) {
                int personalIndex = remaining.indexOf("/personal/");
                int nextSlash = remaining.indexOf("/", personalIndex + 10);
                if (nextSlash != -1) {
                    sitePath = remaining.substring(personalIndex, nextSlash);
                    itemPath = remaining.substring(nextSlash + 1);
                } else {
                    sitePath = remaining.substring(personalIndex);
                }
            }

            if (itemPath.startsWith("Documents/") || itemPath.startsWith("Shared%20Documents/")) {
                itemPath = itemPath.substring(itemPath.indexOf("/") + 1);
            }

            return new String[]{domain, sitePath, itemPath};

        } catch (Exception e) {
            logger.error("Error parsing folder URL: {}", folderUrl, e);
            throw new IllegalArgumentException("Invalid folder URL format", e);
        }
    }

    private String getLastSegment(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String[] segments = path.split("/");
        return segments[segments.length - 1];
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

            FolderNode rootNode = new FolderNode(rootItem.name, rootItem.webUrl);
            listFolderContentsByIdRecursively(client, driveId, rootFolderId, rootNode);

            return rootNode;

        } catch (Exception e) {
            logger.error("Error listing folders and files by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files", e);
        }
    }

    private void validateConfiguration() {
        if (driveId == null || driveId.trim().isEmpty()) {
            throw new IllegalStateException("graph.user.drive-id is not configured");
        }
        if (rootFolderId == null || rootFolderId.trim().isEmpty()) {
            throw new IllegalStateException("graph.user.root-folder-id is not configured");
        }
    }

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

        } catch (Exception e) {
            logger.error("Error listing contents of folder ID '{}': {}", folderId, e.getMessage(), e);
            throw new RuntimeException("Failed to list folder contents: " + folderId, e);
        }
    }

    private void processItemsById(GraphServiceClient<Request> client, String driveId, FolderNode parentNode, DriveItemCollectionPage items) {
        for (DriveItem item : items.getCurrentPage()) {
            if (item.folder != null) {
                FolderNode childNode = new FolderNode(item.name, item.webUrl);
                parentNode.addSubfolder(childNode);
                listFolderContentsByIdRecursively(client, driveId, item.id, childNode);
            } else if (item.file != null) {
                FileNode fileNode = new FileNode(item.name, item.webUrl);
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
            
            logger.info("Starting SharePoint sync with first-level folder matching (dryRun={})", dryRun);
            
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
            
            logger.info("Root folder: {} (driveId={}, folderId={})", rootItem.name, driveId, rootFolderId);
            
            java.util.Set<String> visitedFolders = new java.util.HashSet<>();
            visitedFolders.add(driveId + ":" + rootFolderId);
            
            DriveItemCollectionPage rootChildren = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(rootFolderId)
                    .children()
                    .buildRequest()
                    .get();
            
            if (rootChildren == null || rootChildren.getCurrentPage() == null) {
                logger.warn("No children found in root folder");
                return result;
            }
            
            do {
                int childCount = rootChildren.getCurrentPage().size();
                logger.info("Processing {} first-level children", childCount);
                
                for (DriveItem item : rootChildren.getCurrentPage()) {
                    if (item.folder != null) {
                        processFirstLevelFolder(client, driveId, item.id, item.name, item.webUrl, username, result, dryRun, visitedFolders);
                        
                    } else if (item.remoteItem != null && item.remoteItem.folder != null) {
                        String remoteDriveId = item.remoteItem.parentReference != null && item.remoteItem.parentReference.driveId != null
                                ? item.remoteItem.parentReference.driveId
                                : driveId;
                        String remoteItemId = item.remoteItem.id;
                        String remoteWebUrl = item.remoteItem.webUrl != null ? item.remoteItem.webUrl : item.webUrl;
                        
                        logger.info("Found first-level remote folder (shortcut): {}", item.name);
                        processFirstLevelFolder(client, remoteDriveId, remoteItemId, item.name, remoteWebUrl, username, result, dryRun, visitedFolders);
                    }
                }
                
                if (rootChildren.getNextPage() != null) {
                    logger.debug("Fetching next page of first-level children");
                    rootChildren = rootChildren.getNextPage().buildRequest().get();
                } else {
                    break;
                }
            } while (rootChildren != null && rootChildren.getCurrentPage() != null);
            
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
     * Processes a first-level folder: matches it to a course and traverses its subtree for .mp4 files.
     */
    private void processFirstLevelFolder(GraphServiceClient<Request> client, String driveId, String folderId,
                                         String folderName, String folderWebUrl, String username,
                                         SharePointSyncResult result, boolean dryRun, java.util.Set<String> visitedFolders) {
        try {
            result.setFoldersProcessed(result.getFoldersProcessed() + 1);
            logger.info("Processing first-level folder: {} (driveId={}, folderId={})", folderName, driveId, folderId);
            
            Optional<CourseSummary> courseOpt = matchCourseByFolderPath(folderName, folderWebUrl);
            
            if (courseOpt.isPresent()) {
                CourseSummary course = courseOpt.get();
                result.setCoursesMatched(result.getCoursesMatched() + 1);
                logger.info("Matched first-level folder '{}' to training: {} (ID: {})", folderName, course.getTopics(), course.getTrainingId());
                
                if (course.getFolder_path() == null || !course.getFolder_path().equals(folderWebUrl)) {
                    if (!dryRun) {
                        course.setFolder_path(folderWebUrl);
                        course.setUpdatedBy(username);
                        course.setUpdatedTs(OffsetDateTime.now());
                        courseRepository.save(course);
                    }
                    result.setCoursesUpdated(result.getCoursesUpdated() + 1);
                    logger.info("Updated folder_path for training ID {}: {}", course.getTrainingId(), folderWebUrl);
                }
                
                traverseSubtreeForFiles(client, driveId, folderId, course, username, result, dryRun, visitedFolders);
            } else {
                logger.info("No course matched for first-level folder '{}', traversing subtree but skipping file inserts", folderName);
                traverseSubtreeForFiles(client, driveId, folderId, null, username, result, dryRun, visitedFolders);
            }
            
        } catch (Exception e) {
            logger.error("Error processing first-level folder '{}': {}", folderName, e.getMessage(), e);
            result.addError("Error processing first-level folder '" + folderName + "': " + e.getMessage());
        }
    }
    
    /**
     * Recursively traverses a folder subtree to find and process .mp4 files.
     * Only inserts modules if matchedCourse is not null.
     */
    private void traverseSubtreeForFiles(GraphServiceClient<Request> client, String driveId, String folderId,
                                         CourseSummary matchedCourse, String username,
                                         SharePointSyncResult result, boolean dryRun, java.util.Set<String> visitedFolders) {
        try {
            String folderKey = driveId + ":" + folderId;
            if (visitedFolders.contains(folderKey)) {
                logger.debug("Skipping already visited folder: {}", folderKey);
                return;
            }
            visitedFolders.add(folderKey);
            
            DriveItemCollectionPage items = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(folderId)
                    .children()
                    .buildRequest()
                    .get();
            
            if (items == null || items.getCurrentPage() == null) {
                logger.debug("No children found in folder: {}", folderKey);
                return;
            }
            
            do {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.folder != null) {
                        logger.debug("Traversing subfolder: {} (id={})", item.name, item.id);
                        traverseSubtreeForFiles(client, driveId, item.id, matchedCourse, username, result, dryRun, visitedFolders);
                        
                    } else if (item.remoteItem != null && item.remoteItem.folder != null) {
                        String remoteDriveId = item.remoteItem.parentReference != null && item.remoteItem.parentReference.driveId != null
                                ? item.remoteItem.parentReference.driveId
                                : driveId;
                        String remoteItemId = item.remoteItem.id;
                        
                        logger.debug("Traversing remote subfolder (shortcut): {} (remoteDriveId={}, remoteItemId={})", item.name, remoteDriveId, remoteItemId);
                        traverseSubtreeForFiles(client, remoteDriveId, remoteItemId, matchedCourse, username, result, dryRun, visitedFolders);
                        
                    } else if (item.file != null && item.name != null && item.name.toLowerCase().endsWith(".mp4")) {
                        if (matchedCourse != null) {
                            logger.debug("Found .mp4 file: {} (webUrl={})", item.name, item.webUrl);
                            processVideoFile(item, matchedCourse, username, result, dryRun);
                        } else {
                            logger.debug("Skipping .mp4 file '{}' - no course matched at first level", item.name);
                        }
                    }
                }
                
                if (items.getNextPage() != null) {
                    items = items.getNextPage().buildRequest().get();
                } else {
                    break;
                }
            } while (items != null && items.getCurrentPage() != null);
            
        } catch (Exception e) {
            logger.error("Error traversing subtree for folder '{}': {}", folderKey, e.getMessage(), e);
            result.addError("Error traversing subtree: " + e.getMessage());
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
     * Processes a single video file and inserts/updates course module.
     * The trainingLink (MODULE_PATH) is set to the file's webUrl, which contains the full path to the video file.
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
