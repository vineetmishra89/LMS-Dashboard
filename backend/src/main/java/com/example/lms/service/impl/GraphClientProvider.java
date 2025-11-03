package com.example.lms.service.impl;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    
    /**
     * Creates a Microsoft Graph client using a provided bearer token (delegated user token).
     * This method does NOT cache the client - a new client is created for each call.
     * Use this when you have a user's access token from Authorization header.
     * 
     * @param bearerToken The access token (without "Bearer " prefix)
     * @return Configured GraphServiceClient instance for this token
     * @throws RuntimeException if client initialization fails
     */
    public GraphServiceClient<Request> getGraphClientWithBearerToken(String bearerToken) {
        try {
            logger.debug("Creating Microsoft Graph client with provided bearer token");
            
            Instant expiresAt = Instant.now().plusSeconds(3600);
            
            TokenCredential tokenCredential = new TokenCredential() {
                @Override
                public Mono<AccessToken> getToken(TokenRequestContext request) {
                    return Mono.just(new AccessToken(bearerToken, OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC)));
                }
            };
            
            TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                    GRAPH_SCOPES, tokenCredential);
            
            GraphServiceClient<Request> client = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
            
            logger.debug("Microsoft Graph client created successfully with bearer token");
            return client;
            
        } catch (Exception e) {
            logger.error("Failed to create Microsoft Graph client with bearer token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Microsoft Graph client with bearer token", e);
        }
    }
}
