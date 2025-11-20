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
     * Extracts email from JWT token.
     * Tries claims in order: email, preferred_username, upn, sub, unique_name, username.
     * Only returns values that look like valid email addresses (contains @ and no spaces).
     * 
     * @param bearerToken JWT token (without "Bearer " prefix)
     * @return Email address if found and valid, null otherwise
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
            
            String[] claimNames = {"email", "preferred_username", "upn", "sub", "unique_name", "username"};
            
            for (String claimName : claimNames) {
                String value = extractClaim(claims, claimName);
                if (value != null && isValidEmail(value)) {
                    logger.debug("Extracted email from '{}' claim: {}", claimName, value);
                    return value;
                }
                if (value != null && !isValidEmail(value)) {
                    logger.debug("Skipping '{}' claim - value '{}' is not a valid email", claimName, value);
                }
            }
            
            logger.warn("No valid email claim found in JWT token. Available claims: {}", 
                    String.join(", ", claimNames));
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting email from JWT: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Validates if a string looks like a valid email address.
     * Checks for presence of @ and absence of spaces.
     * 
     * @param value String to validate
     * @return true if value looks like an email, false otherwise
     */
    private static boolean isValidEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.contains("@") && !value.contains(" ");
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
