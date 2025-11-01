package com.example.lms.service.impl;

import com.example.lms.service.SharePointService;
import com.example.lms.util.PathUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    private  ObjectMapper objectMapper;

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
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJub25jZSI6Il95TzUxSC11WG1pVkZ6S3RVZXhub1labk40ZlMxWWRkb01ZR3g0Z29EeWMiLCJhbGciOiJSUzI1NiIsIng1dCI6InlFVXdtWFdMMTA3Q2MtN1FaMldTYmVPYjNzUSIsImtpZCI6InlFVXdtWFdMMTA3Q2MtN1FaMldTYmVPYjNzUSJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC83M2JmZmUyYi05MDQxLTQ3NTQtYWFmMC0zZWY2MWNkZTc1NTkvIiwiaWF0IjoxNzYxOTkyNTQ3LCJuYmYiOjE3NjE5OTI1NDcsImV4cCI6MTc2MTk5NzA2OCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFVUUF1LzhhQUFBQW5nMU51NzNlVFcwUGNpcldDT0NGWkpLdVJ2TXhPQUJTNm02cVcxUStYT2h5cTdnalFKNDB4cmhHK2xycW9nc3NiUlRyUnJVbTlvSXdhcTBqZ3RpUmhnPT0iLCJhbXIiOlsicHdkIiwicnNhIl0sImFwcF9kaXNwbGF5bmFtZSI6ImRlc2lnbiBMZWFybmluZyAiLCJhcHBpZCI6IjYyMTg5Zjk2LTIwZWMtNDJiNy04ZmZmLWJjZWFmZTc3YWZkNSIsImFwcGlkYWNyIjoiMCIsImRldmljZWlkIjoiMGU4NjhhOWQtMzlkOC00MjZhLTkwZDUtODQ3NTI4YjRlMzhjIiwiZmFtaWx5X25hbWUiOiJCYW5zYWwiLCJnaXZlbl9uYW1lIjoiQW5raXQiLCJpZHR5cCI6InVzZXIiLCJpcGFkZHIiOiIyNDAxOjQ5MDA6MWM2Mjo4MWQ1OjExYWU6ZjQ1ZjpkMDAzOmY4MjIiLCJuYW1lIjoiQW5raXQgQmFuc2FsIiwib2lkIjoiNTJhMjE0Y2QtNGM1Zi00Y2U3LTg0NTMtYjNiNTkyOWI0ZDI3Iiwib25wcmVtX3NpZCI6IlMtMS01LTIxLTIxNTUwMTc2MDAtMTI3MTc4MDQ1Ni0xMTYxOTkzMDA1LTMzODAxIiwicGxhdGYiOiIzIiwicHVpZCI6IjEwMDMyMDAyMDRGMDZGMUUiLCJyaCI6IjEuQVZJQUtfNl9jMEdRVkVlcThENzJITjUxV1FNQUFBQUFBQUFBd0FBQUFBQUFBQUJTQURoU0FBLiIsInNjcCI6IkRpcmVjdG9yeS5SZWFkLkFsbCBGaWxlcy5SZWFkLkFsbCBvcGVuaWQgcHJvZmlsZSBTaXRlcy5GdWxsQ29udHJvbC5BbGwgVXNlci5SZWFkIGVtYWlsIiwic2lkIjoiMDA5YzQ3ZTktYTQwNy05NDdiLWY0ZmItMzRlZDcwYzA3MzgzIiwic2lnbmluX3N0YXRlIjpbImR2Y19tbmdkIiwiZHZjX2NtcCIsImR2Y19kbWpkIiwia21zaSJdLCJzdWIiOiJ0bExWT3R0ajY0M3ZIaXlGbjFXTHRGOGRxUnJGZDJ6MFg3QnRYZlRLcG93IiwidGVuYW50X3JlZ2lvbl9zY29wZSI6Ik5BIiwidGlkIjoiNzNiZmZlMmItOTA0MS00NzU0LWFhZjAtM2VmNjFjZGU3NTU5IiwidW5pcXVlX25hbWUiOiJhbmtpdC5iYW5zYWxAaXJpc3NvZnR3YXJlLmNvbSIsInVwbiI6ImFua2l0LmJhbnNhbEBpcmlzc29mdHdhcmUuY29tIiwidXRpIjoidHNHVWdrbU54RW05MUt2ZTZYUlhBQSIsInZlciI6IjEuMCIsIndpZHMiOlsiYjc5ZmJmNGQtM2VmOS00Njg5LTgxNDMtNzZiMTk0ZTg1NTA5Il0sInhtc19hY2QiOjE3NTA2NzAyMjIsInhtc19hY3RfZmN0IjoiMyA5IiwieG1zX2Z0ZCI6IjM1d2Fkd0Y0bXZaYzRSb0NmY2xpUXhQdXB4LTZOblk2bElMS2VmLXZBVTRCZFhObFlYTjBMV1J6YlhNIiwieG1zX2lkcmVsIjoiMSA0IiwieG1zX3N0Ijp7InN1YiI6IlladXVVX3QyRzNVbDBodkl2eTlGTHphTkdkczNJUlptYTVEY09vN3hMMG8ifSwieG1zX3N1Yl9mY3QiOiIxMiAzIiwieG1zX3RjZHQiOjE1OTExOTU5NzAsInhtc190bnRfZmN0IjoiNiAzIn0.dFwv1WbD5u6X9U1RMtbJ270_vZ49HsdTUW4DXjue8EkxayxgHqLHkLhdoMvOXsdt87iURVre3aRSrMWIrCs_imHfLYlYfPnyl2JbI8b_Z-ph45vuxow8JFdHQbMjjgkzEtVBysa-JhU0hFegCjGndKXQh1WdMNRSE9cbY4cBjtw_BoXiMvvBQRxQBC3poq3puaIo1L8mm3t_gUxVf8YPkouFbaI1JUMQa2Tyb8ENm49lttOwvKfgn3TeOdnJrCnwt60ZaCYCjfPrGFS1K0amwQpm807Bddl_e_sZ5imhtW_--pqOYF9WwRzwfBAV-R62EJ0_pBjB_FI4Ky6B848SmA");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // Get children of specific drive item
                String url = String.format("https://graph.microsoft.com/v1.0/drives/%s/items/%s/children", "b!h-u0gl1vu0mtETS610zX9hufYTIu9hZJjUnn3YdGkBYfslJiWknLTrSquiV92Sgm", "015ZUXCKF5VF3V734C3JG3SQ4RE5RHPGBY");
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return parseFilesFromResponse(response.getBody());
                }

                return new ArrayList<>();
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch drive item children from Graph API: " + e.getMessage());
            }
    }
    
    private List<String> parseFilesFromResponse(String responseBody) {
        List<String> files = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode valueNode = root.get("value");

            if (valueNode != null && valueNode.isArray()) {
                for (JsonNode fileNode : valueNode) {
                	files.add(fileNode.get("name").asText());

                 
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse files response: " + e.getMessage());
        }

        return files;
    }

    
    /**
     * Normalizes folder input to handle both SharePoint web URLs and relative paths.
     * 
     * Supports:
     * - Full SharePoint URLs like: https://irissoft-my.sharepoint.com/:f:/r/personal/user/Documents/Folder
     * - OneDrive URLs with query params: https://.../_layouts/15/onedrive.aspx?id=%2Fpersonal%2F...
     * - Relative paths like: "Folder Name" or "Subfolder/Nested Folder"
     * 
     * @param folderInput Raw folder input from user (URL or relative path)
     * @return Normalized relative path (decoded, without personal/ prefix)
     */
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
                    extractedPath = path.substring(rIndex + 3); // Skip "/r/"
                } else {
                    extractedPath = path.substring(fIndex + 5); // Skip "/:f:/"
                }
                
                extractedPath = URLDecoder.decode(extractedPath, StandardCharsets.UTF_8);
                logger.debug("Extracted path from /:f:/r/ format: {}", extractedPath);
            }
            else if (uri.getQuery() != null && uri.getQuery().contains("id=")) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        String idValue = param.substring(3); // Skip "id="
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
            
            if (extractedPath.startsWith("personal/")) {
                int secondSlash = extractedPath.indexOf('/', 9); // Find slash after "personal/"
                if (secondSlash != -1) {
                    extractedPath = extractedPath.substring(secondSlash + 1);
                    logger.debug("Stripped personal/ prefix, result: {}", extractedPath);
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
    
    @Override
    public String testFolderAccess(String folderPath) {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== SharePoint OneDrive Folder Access Test ===\n\n");
        
        try {
            diagnostics.append("Configuration:\n");
            diagnostics.append("  - User Principal Name: ").append(userPrincipalName).append("\n");
            diagnostics.append("  - Base Path: ").append(basePath).append("\n");
            diagnostics.append("  - Mode: OneDrive for Business\n\n");
            
            diagnostics.append("Input:\n");
            diagnostics.append("  - Raw folder path: ").append(folderPath).append("\n\n");
            
            String normalizedPath = normalizeFolderInput(folderPath);
            diagnostics.append("Normalization:\n");
            diagnostics.append("  - Normalized path: ").append(normalizedPath).append("\n\n");
            
            String fullPath = PathUtils.combine(basePath, normalizedPath);
            diagnostics.append("Resolved Path:\n");
            diagnostics.append("  - Full OneDrive path: ").append(fullPath).append("\n\n");
            
            diagnostics.append("Graph API Call:\n");
            String graphUrl = String.format("GET /users/%s/drive/root:/%s:/children", userPrincipalName, fullPath);
            diagnostics.append("  - Endpoint: ").append(graphUrl).append("\n\n");
            
            logger.info("Testing folder access for path: {}", folderPath);
            GraphServiceClient<Request> client = graphClientProvider.getGraphClient();
            
            diagnostics.append("Authentication: SUCCESS\n");
            diagnostics.append("  - Graph client initialized\n");
            diagnostics.append("  - Token acquired\n\n");
            
            DriveItemCollectionPage items = client
                    .users(userPrincipalName)
                    .drive()
                    .root()
                    .itemWithPath(fullPath)
                    .children()
                    .buildRequest()
                    .get();
            
            diagnostics.append("Folder Access: SUCCESS\n");
            
            int fileCount = 0;
            int folderCount = 0;
            List<String> sampleFiles = new ArrayList<>();
            
            if (items != null && items.getCurrentPage() != null) {
                for (DriveItem item : items.getCurrentPage()) {
                    if (item.file != null) {
                        fileCount++;
                        if (sampleFiles.size() < 5) {
                            sampleFiles.add(item.name);
                        }
                    } else if (item.folder != null) {
                        folderCount++;
                    }
                }
            }
            
            diagnostics.append("  - Files found: ").append(fileCount).append("\n");
            diagnostics.append("  - Folders found: ").append(folderCount).append("\n");
            
            if (!sampleFiles.isEmpty()) {
                diagnostics.append("  - Sample files (up to 5):\n");
                for (String fileName : sampleFiles) {
                    diagnostics.append("    * ").append(fileName).append("\n");
                }
            }
            
            diagnostics.append("\n=== TEST PASSED ===\n");
            logger.info("Folder access test PASSED for path: {}", folderPath);
            
            return diagnostics.toString();
            
        } catch (Exception e) {
            diagnostics.append("Folder Access: FAILED\n");
            diagnostics.append("  - Error Type: ").append(e.getClass().getSimpleName()).append("\n");
            diagnostics.append("  - Error Message: ").append(e.getMessage()).append("\n");
            
            if (e.getCause() != null) {
                diagnostics.append("  - Cause: ").append(e.getCause().getMessage()).append("\n");
            }
            
            diagnostics.append("\n=== TEST FAILED ===\n");
            diagnostics.append("\nFull Error Details:\n");
            diagnostics.append(e.toString()).append("\n");
            
            logger.error("Folder access test FAILED for path: {}", folderPath, e);
            
            return diagnostics.toString();
        }
    }
    
    @Override
    public String listSiteDrives() {
        throw new UnsupportedOperationException("listSiteDrives() is only supported in SharePoint site mode. " +
                "Current mode is OneDrive for Business. To list drives, switch to graph.mode=site.");
    }
}
