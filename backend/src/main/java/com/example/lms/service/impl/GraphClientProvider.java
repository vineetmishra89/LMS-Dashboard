package com.example.lms.service.impl;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provider component for Microsoft Graph API client.
 * Centralizes Graph client creation and authentication using client credentials flow.
 * Shared by all SharePoint service implementations.
 */
@Component
public class GraphClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(GraphClientProvider.class);
    private static final List<String> GRAPH_SCOPES = List.of("https://graph.microsoft.com/.default");

    @Value("${graph.tenant-id}")
    private String tenantId;

    @Value("${graph.client-id}")
    private String clientId;

    @Value("${graph.client-secret}")
    private String clientSecret;

    private GraphServiceClient<Request> graphClient;

    /**
     * Gets or creates a Microsoft Graph client with client credentials authentication.
     * The client is created lazily on first use and reused for subsequent calls.
     * 
     * @return Configured GraphServiceClient instance
     * @throws RuntimeException if client initialization fails
     */
    public GraphServiceClient<Request> getGraphClient() {
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
     * Validates that all required authentication properties are set.
     * 
     * @throws IllegalStateException if any required property is missing
     */
    public void validateAuthConfiguration() {
        if (tenantId == null || tenantId.trim().isEmpty() || tenantId.contains("your-")) {
            throw new IllegalStateException("Missing required property: graph.tenant-id");
        }
        if (clientId == null || clientId.trim().isEmpty() || clientId.contains("your-")) {
            throw new IllegalStateException("Missing required property: graph.client-id");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty() || clientSecret.contains("your-")) {
            throw new IllegalStateException("Missing required property: graph.client-secret");
        }
    }
}
