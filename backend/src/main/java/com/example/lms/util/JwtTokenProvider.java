package com.example.lms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating and validating JWT tokens.
 * Uses HS256 algorithm with a secret key for signing.
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    @Value("${jwt.issuer}")
    private String jwtIssuer;
    
    /**
     * Generates a JWT token for the given user.
     * 
     * @param emailId User's email ID
     * @param name User's name
     * @param role User's role
     * @return JWT token string
     */
    public String generateToken(String emailId, String name, String role) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", emailId);  // Add email claim for robust extraction
            claims.put("preferred_username", emailId);  // Add for AAD compatibility
            claims.put("name", name);
            claims.put("role", role);
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            String token = Jwts.builder()
                    .subject(emailId)  // Email also in standard 'sub' claim
                    .claims(claims)
                    .issuer(jwtIssuer)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(key)
                    .compact();
            
            logger.info("Generated JWT token for user: {} with claims: email, preferred_username, name, role", emailId);
            return token;
            
        } catch (Exception e) {
            logger.error("Error generating JWT token for user {}: {}", emailId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
    
    /**
     * Extracts email ID from JWT token.
     * 
     * @param token JWT token
     * @return Email ID (subject claim)
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validates JWT token.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            
            logger.debug("JWT token validation successful");
            return true;
            
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Gets expiration date from token.
     * 
     * @param token JWT token
     * @return Expiration date as OffsetDateTime
     */
    public OffsetDateTime getExpirationFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return OffsetDateTime.ofInstant(expiration.toInstant(), ZoneOffset.UTC);
        } catch (Exception e) {
            logger.error("Error extracting expiration from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts all claims from JWT token.
     * 
     * @param token JWT token
     * @return Claims object
     */
    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
