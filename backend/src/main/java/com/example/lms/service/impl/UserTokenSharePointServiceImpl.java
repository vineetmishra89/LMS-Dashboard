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
    static int levelCount = 1;
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
            validateConfiguration();

            logger.info("Listing folders and files recursively from URL: {}", folderUrl);

            String relativePath = parseFolderUrl(folderUrl);
            logger.info("Parsed relative path: {}", relativePath);

            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);

            String targetFolderId = resolveFolderIdFromPath(client, driveId, rootFolderId, relativePath);
            logger.info("Resolved folder ID: {}", targetFolderId);

            FolderNode rootFolder = new FolderNode();
            rootFolder.setName(getLastSegment(relativePath));
            rootFolder.setPath(relativePath);

            listFolderContentsByIdRecursively(client, driveId, targetFolderId, rootFolder);

            logger.info("Successfully listed folders and files recursively");
            return rootFolder;

        } catch (Exception e) {
            logger.error("Error listing folders and files recursively: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves a relative folder path to a folder ID by walking the path segments.
     * Starts from the given startFolderId and navigates through child folders by name.
     * Uses the same drive access pattern as PR #53: drives().byId().items().byId().children()
     *
     * @param client Graph client with bearer token
     * @param driveId The drive ID to search in
     * @param startFolderId The folder ID to start navigation from (typically rootFolderId)
     * @param relativePath The relative path to resolve (e.g., "Documents/Recordings/Training")
     * @return The folder ID of the target folder
     * @throws RuntimeException if the path cannot be resolved
     */
    private String resolveFolderIdFromPath(GraphServiceClient<Request> client, String driveId,
                                           String startFolderId, String relativePath) {
        try {
            if (relativePath == null || relativePath.trim().isEmpty()) {
                return startFolderId;
            }

            String[] segments = relativePath.split("/");
            String currentFolderId = startFolderId;

            for (String segment : segments) {
                if (segment.trim().isEmpty()) {
                    continue;
                }

                logger.debug("Resolving path segment: '{}' in folder ID: {}", segment, currentFolderId);

                DriveItemCollectionPage items = client
                        .drives()
                        .byId(driveId)
                        .items()
                        .byId(currentFolderId)
                        .children()
                        .buildRequest()
                        .get();

                if (items == null || items.getCurrentPage() == null) {
                    throw new RuntimeException("No items found in folder ID: " + currentFolderId);
                }

                String nextFolderId = null;
                do {
                    for (DriveItem item : items.getCurrentPage()) {
                        if (item.folder != null && item.name.equals(segment)) {
                            nextFolderId = item.id;
                            logger.debug("Found matching folder: '{}' with ID: {}", segment, nextFolderId);
                            break;
                        }
                    }

                    if (nextFolderId != null) {
                        break;
                    }

                    if (items.getNextPage() != null) {
                        items = items.getNextPage().buildRequest().get();
                    } else {
                        break;
                    }
                } while (items != null && items.getCurrentPage() != null);

                if (nextFolderId == null) {
                    throw new RuntimeException("Folder not found: '" + segment + "' in path: " + relativePath);
                }

                currentFolderId = nextFolderId;
            }

            return currentFolderId;

        } catch (Exception e) {
            logger.error("Error resolving folder path '{}': {}", relativePath, e.getMessage(), e);
            throw new RuntimeException("Failed to resolve folder path: " + relativePath, e);
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

            FolderNode rootNode = new FolderNode(rootItem.name, rootFolderId, rootItem.webUrl);
            listFolderContentsByIdRecursively(client, driveId, rootFolderId, rootNode);

            return rootNode;

        } catch (Exception e) {
            logger.error("Error listing folders and files by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files", e);
        }
    }

    @Override
    public FolderNode listFoldersAndFilesRecursivelyByPath(String bearerToken, String folderPath) {
        try {
            validateConfiguration();

            logger.info("Listing folders and files recursively using folderPath: {}", folderPath);

            if (folderPath == null || folderPath.trim().isEmpty()) {
                throw new IllegalArgumentException("Folder path cannot be null or empty");
            }

            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);

            String targetFolderId = resolveFolderIdFromPath(client, driveId, rootFolderId, folderPath);
            logger.info("Resolved folder ID: {} for folderPath: {}", targetFolderId, folderPath);

            DriveItem targetItem = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(targetFolderId)
                    .buildRequest()
                    .get();

            if (targetItem == null) {
                throw new RuntimeException("Target folder not found with ID: " + targetFolderId);
            }

            FolderNode rootNode = new FolderNode(targetItem.name, targetFolderId, targetItem.webUrl);
            listFolderContentsByIdRecursively(client, driveId, targetFolderId, rootNode);

            logger.info("Successfully listed folders and files for folderPath: {}", folderPath);
            return rootNode;

        } catch (Exception e) {
            logger.error("Error listing folders and files by path '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Failed to list folders and files for path: " + folderPath, e);
        }
    }

    @Override
    public java.util.List<String> fetchAllFilePathsFromWebUrl(String bearerToken, String webUrl) {
        try {
            validateConfiguration();

            logger.info("Fetching all file paths from webUrl: {}", webUrl);

            if (webUrl == null || webUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("WebUrl cannot be null or empty");
            }

            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);

            String relativePath = parseFolderUrl(webUrl);
            logger.info("Parsed relative path from webUrl: {}", relativePath);

            String targetFolderId = resolveFolderIdFromPath(client, driveId, rootFolderId, relativePath);
            logger.info("Resolved folder ID: {} for webUrl: {}", targetFolderId, webUrl);

            java.util.List<String> filePaths = new java.util.ArrayList<>();
            collectFilePathsRecursively(client, driveId, targetFolderId, filePaths);

            logger.info("Successfully fetched {} file paths from webUrl: {}", filePaths.size(), webUrl);
            return filePaths;

        } catch (Exception e) {
            logger.error("Error fetching file paths from webUrl '{}': {}", webUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch file paths from webUrl: " + webUrl, e);
        }
    }

    private void collectFilePathsRecursively(GraphServiceClient<Request> client, String driveId, String folderId, java.util.List<String> filePaths) {
        try {
            logger.debug("Collecting file paths from folder ID: {}", folderId);

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

            do {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.folder != null) {
                        logger.debug("Recursing into subfolder: {} (id={})", item.name, item.id);
                        collectFilePathsRecursively(client, driveId, item.id, filePaths);
                    } else if (item.file != null) {
                        logger.debug("Found file: {} with webUrl: {}", item.name, item.webUrl);
                        filePaths.add(item.webUrl);
                    }
                }

                if (items.getNextPage() != null) {
                    items = items.getNextPage().buildRequest().get();
                } else {
                    break;
                }
            } while (items != null && items.getCurrentPage() != null);

        } catch (Exception e) {
            logger.error("Error collecting file paths from folder ID '{}': {}", folderId, e.getMessage(), e);
            throw new RuntimeException("Failed to collect file paths from folder: " + folderId, e);
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
            logger.info("Listing contents of folder ID: {}", folderId);

            DriveItemCollectionPage items = client
                    .drives()
                    .byId(driveId)
                    .items()
                    .byId(folderId)
                    .children()
                    .buildRequest()
                    .get();

            if (items == null || items.getCurrentPage() == null) {
                logger.info("No items found in folder ID: {}", folderId);
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
            if (item.folder != null && levelCount == 1) {
                FolderNode childNode = new FolderNode(item.name, item.id, item.webUrl);
                parentNode.addFolder(childNode);
                listFolderContentsByIdRecursively(client, driveId, item.id, childNode);
                ++levelCount;

            } else if (item.file != null) {
                FileNode fileNode = new FileNode(item.name, item.id, item.webUrl, item.size, item.lastModifiedDateTime);
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
            logger.info("Completed matchCourseByFolderPath :: courseOpt.isPresent(): {}", courseOpt.isPresent());

            if (courseOpt.isPresent()) {
                CourseSummary course = courseOpt.get();
                result.setCoursesMatched(result.getCoursesMatched() + 1);
                logger.info("Matched first-level folder '{}' to training: {} (ID: {})", folderName, course.getTopics(), course.getTrainingId());

                if (course.getFolderPath() == null || !course.getFolderPath().equals(folderWebUrl)) {
                    if (!dryRun) {
                        course.setFolderPath(folderWebUrl);
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
      int seqId = 1;
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
                        logger.info("Traversing subfolder: {} (id={})", item.name, item.id);
                        //traverseSubtreeForFiles(client, driveId, item.id, matchedCourse, username, result, dryRun, visitedFolders);

                    } else if (item.remoteItem != null && item.remoteItem.folder != null) {
                        String remoteDriveId = item.remoteItem.parentReference != null && item.remoteItem.parentReference.driveId != null
                                ? item.remoteItem.parentReference.driveId
                                : driveId;
                        String remoteItemId = item.remoteItem.id;

                        logger.debug("Traversing remote subfolder (shortcut): {} (remoteDriveId={}, remoteItemId={})", item.name, remoteDriveId, remoteItemId);
                        //traverseSubtreeForFiles(client, remoteDriveId, remoteItemId, matchedCourse, username, result, dryRun, visitedFolders);

                    } else if (item.file != null && item.name != null && item.name.toLowerCase().endsWith(".mp4")) {
                        if (matchedCourse != null) {
                            logger.info("Found .mp4 file: {} (webUrl={})", item.name, item.webUrl);
                            processVideoFile(item, matchedCourse, username, result, dryRun, seqId++);
                        } else {
                            logger.info("Skipping .mp4 file '{}' - no course matched at first level", item.name);
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
            String folderKeyForLog = driveId + ":" + folderId;
            logger.error("Error traversing subtree for folder '{}': {}", folderKeyForLog, e.getMessage(), e);
            result.addError("Error traversing subtree: " + e.getMessage());
        }
    }

    /**
     * Matches CourseSummary by folder_path (supports both name and URL matching).
     */
    private Optional<CourseSummary> matchCourseByFolderPath(String folderName, String folderWebUrl) {
        Optional<CourseSummary> courseOpt = courseRepository.findByFolderPath(folderWebUrl);
        if (courseOpt.isPresent()) {
            logger.info("Matched by URL: {}", folderWebUrl);
            return courseOpt;
        }

        courseOpt = courseRepository.findByFolderPathIgnoreCase(folderName);
        if (courseOpt.isPresent()) {
            logger.info("Matched by name: {}", folderName);
            return courseOpt;
        }

        return Optional.empty();
    }

    /**
     * Processes a single video file and inserts/updates course module.
     * The trainingLink (MODULE_PATH) is set to the file's webUrl, which contains the full path to the video file.
     */
    private void processVideoFile(DriveItem item, CourseSummary course, String username,
                                   SharePointSyncResult result, boolean dryRun, int seqId) {
        try {
            String filename = item.name;
            String webUrl = item.webUrl;

            String moduleName = SessionSequenceParser.removeExtension(filename);
          //Integer seqId = SessionSequenceParser.extractSessionNumber(filename);

            logger.info("Processing file: {} -> moduleName={}, seqId={}, webUrl={}", filename, moduleName, seqId, webUrl);

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

                if (!Integer.valueOf(seqId).equals(existingModule.getSeqId())) {
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
