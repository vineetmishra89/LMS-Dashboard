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
                headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJub25jZSI6ImJNN0tkUmFORVgxaDR2WlVjZVVOdzd0SEZxUEowZlhkb0s4ZTg4aVJ5ZHMiLCJhbGciOiJSUzI1NiIsIng1dCI6InlFVXdtWFdMMTA3Q2MtN1FaMldTYmVPYjNzUSIsImtpZCI6InlFVXdtWFdMMTA3Q2MtN1FaMldTYmVPYjNzUSJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC83M2JmZmUyYi05MDQxLTQ3NTQtYWFmMC0zZWY2MWNkZTc1NTkvIiwiaWF0IjoxNzYyMzQ3MzE1LCJuYmYiOjE3NjIzNDczMTUsImV4cCI6MTc2MjM1MjAxMCwiYWNjdCI6MCwiYWNyIjoiMSIsImFjcnMiOlsicDEiXSwiYWlvIjoiQVhRQWkvOGFBQUFBMTRaVUNueHE0U2ZrdjhwNlRkSlpUSnliNUJ1VDFQNW4wTWRzODMwMEl3YlRRVmcwbjhEZk0zK24xZDZRSmYvQXlRV2Z1YWlldFM2cVp5QmFUMXBiQ2gxTkpDVUdDNzBScFRVa2dITWRaQ0s0MmRHY1plZzUwdzdGODErbzR0L0p3UHpIZE1SbDhWa0RoemlOWTRON1p3PT0iLCJhbXIiOlsicHdkIiwicnNhIiwibWZhIl0sImFwcF9kaXNwbGF5bmFtZSI6ImRlc2lnbiBMZWFybmluZyAiLCJhcHBpZCI6IjYyMTg5Zjk2LTIwZWMtNDJiNy04ZmZmLWJjZWFmZTc3YWZkNSIsImFwcGlkYWNyIjoiMCIsImRldmljZWlkIjoiMzQ4NGZjZGQtZmMyNC00NWFkLTljNzgtYzcyMDlhYmZkNTAxIiwiZmFtaWx5X25hbWUiOiJCaGF0dGFjaGFyeWEiLCJnaXZlbl9uYW1lIjoiUmFqaWIiLCJpZHR5cCI6InVzZXIiLCJpcGFkZHIiOiIxMjIuMTYxLjY0Ljc4IiwibmFtZSI6IlJhamliIEJoYXR0YWNoYXJ5YSIsIm9pZCI6IjZjNTEyM2Y0LWM1ODItNGE4NC1hMjM0LWJlMDI3MTk5M2Y4MyIsIm9ucHJlbV9zaWQiOiJTLTEtNS0yMS0yMTU1MDE3NjAwLTEyNzE3ODA0NTYtMTE2MTk5MzAwNS0yMDI3MSIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzMjAwMENEOTI4NUM0IiwicmgiOiIxLkFWSUFLXzZfYzBHUVZFZXE4RDcySE41MVdRTUFBQUFBQUFBQXdBQUFBQUFBQUFCU0FLZFNBQS4iLCJzY3AiOiJBbGxTaXRlcy5GdWxsQ29udHJvbCBEaXJlY3RvcnkuUmVhZC5BbGwgRmlsZXMuUmVhZC5BbGwgTXlGaWxlcy5SZWFkIE15RmlsZXMuV3JpdGUgb3BlbmlkIHByb2ZpbGUgU2l0ZXMuRnVsbENvbnRyb2wuQWxsIFNpdGVzLlNlYXJjaC5BbGwgU2l0ZXMuU2VsZWN0ZWQgVXNlci5SZWFkIFVzZXIuUmVhZC5BbGwgVXNlci5SZWFkV3JpdGUuQWxsIGVtYWlsIiwic2lkIjoiMDA4YjA5ZDktY2QyYi1iZDk0LTFhZWEtZTVmZjMxMmYxODZlIiwic2lnbmluX3N0YXRlIjpbImR2Y19tbmdkIiwiZHZjX2RtamQiLCJrbXNpIl0sInN1YiI6Il9tcm00YTNENHRRQ1FJNkJONVljTE1jQlltVk9WOVBhOHNSeGg2UHVSelUiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiTkEiLCJ0aWQiOiI3M2JmZmUyYi05MDQxLTQ3NTQtYWFmMC0zZWY2MWNkZTc1NTkiLCJ1bmlxdWVfbmFtZSI6InJhamliLmJoYXR0YWNoYXJ5YUBpcmlzc29mdHdhcmUuY29tIiwidXBuIjoicmFqaWIuYmhhdHRhY2hhcnlhQGlyaXNzb2Z0d2FyZS5jb20iLCJ1dGkiOiJHb2xPSm5kbXAwMmpqNndoenBRUEFRIiwidmVyIjoiMS4wIiwid2lkcyI6WyJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX2FjZCI6MTc1MDY3MDIyMiwieG1zX2FjdF9mY3QiOiIzIDkiLCJ4bXNfZnRkIjoiV1NpX0F6RTdjcFd4VzE4SGduQzR2a0JldTVoRVBhc0dWdzJ3X1ppWWc0Y0JkWE56YjNWMGFDMWtjMjF6IiwieG1zX2lkcmVsIjoiMTAgMSIsInhtc19zdCI6eyJzdWIiOiJHVmNTMUFlQjBhN3FIbHRkWjFsREU2TU0zNFVkUkVSVnVZVnNwS1h2YXZVIn0sInhtc19zdWJfZmN0IjoiMiAzIiwieG1zX3RjZHQiOjE1OTExOTU5NzAsInhtc190bnRfZmN0IjoiMyA0In0.lyGvDXLXUQIK2zOGOwVVyD73fxMLXTLln__XSTv6LABYSHPGoIUfrU9l3HNRTZo4gv6BDphDPeX1tUC8CrVWSWKKql8F5pMj74aFszb6Je8x0CyWFO6ROhS5yTlyF5UEnz7TIzLmA7IK-Eiob-MbWCCGnzPBrZJejUZ9pfc5-HYsYGGxyzop989g2KScOF0evbsMzTZQ3VXWq7RI8OBXx4_KJziVp7fn7cCHV0V33RbgHygVPuXWFhX70kR5Iz613Tc2kuP_U_vIsLMPrRVUAFBJqpRlMXGLOLuK9V3yy5w9U6odBEUR24lnPuWGgdubNQO9h98onkRYwk8K7CBCpw");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // Get children of specific drive item
                //String url = String.format("https://graph.microsoft.com/v1.0/drives/%s/items/%s/children", "b!h-u0gl1vu0mtETS610zX9hufYTIu9hZJjUnn3YdGkBYfslJiWknLTrSquiV92Sgm", "015ZUXCKF5VF3V734C3JG3SQ4RE5RHPGBY");
                String url = String.format("https://graph.microsoft.com/v1.0/drives/%s/items/%s/children", "b!h-u0gl1vu0mtETS610zX9hufYTIu9hZJjUnn3YdGkBYfslJiWknLTrSquiV92Sgm", "015ZUXCKADVIAVTQVOGRCKAQ2IRZAYGZJ7");
                logger.info("url === {}",url);
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
