package com.example.lms.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Utility class for extracting claims from JWT tokens.
 * Performs simple Base64 decoding without signature verification.
 * Used for extracting username from bearer tokens.
 */
public class JwtClaimExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtClaimExtractor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Extracts username from JWT token.
     * Tries claims in order: preferred_username, upn, email, name.
     * 
     * @param bearerToken JWT token (without "Bearer " prefix)
     * @return Username if found, null otherwise
     */
    public static String extractUsername(String bearerToken) {
        try {
            if (bearerToken == null || bearerToken.trim().isEmpty()) {
                logger.warn("Bearer token is null or empty");
                return null;
            }
            
            String[] parts = bearerToken.split("\\.");
            if (parts.length < 2) {
                logger.warn("Invalid JWT format: expected 3 parts, got {}", parts.length);
                return null;
            }
            
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes);
            
            JsonNode claims = objectMapper.readTree(decodedPayload);
            
            String username = extractClaim(claims, "preferred_username");
            if (username != null) {
                logger.debug("Extracted username from preferred_username: {}", username);
                return username;
            }
            
            username = extractClaim(claims, "upn");
            if (username != null) {
                logger.debug("Extracted username from upn: {}", username);
                return username;
            }
            
            username = extractClaim(claims, "email");
            if (username != null) {
                logger.debug("Extracted username from email: {}", username);
                return username;
            }
            
            username = extractClaim(claims, "name");
            if (username != null) {
                logger.debug("Extracted username from name: {}", username);
                return username;
            }
            
            logger.warn("No username claim found in JWT token");
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting username from JWT: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extracts a claim value from JSON claims.
     */
    private static String extractClaim(JsonNode claims, String claimName) {
        if (claims.has(claimName) && !claims.get(claimName).isNull()) {
            return claims.get(claimName).asText();
        }
        return null;
    }
}
