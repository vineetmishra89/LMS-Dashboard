package com.example.lms.controller;

import com.example.lms.dto.UserRolesDTO;
import com.example.lms.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for user role operations.
 * Provides endpoints to retrieve user roles for authorization and UI visibility.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserRoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    
    private final UserRoleService userRoleService;
    
    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }
    
    /**
     * Get all roles for the authenticated user.
     * Returns a list of role strings that can be used for:
     * - UI visibility control (showing/hiding menu items)
     * - Frontend route guards
     * - Authorization checks
     * 
     * The user's email is extracted from the JWT token or security context.
     * 
     * @return UserRolesDTO containing list of role strings
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getUserRoles() {
        try {
            String emailId = getAuthenticatedUserEmail();
            
            if (emailId == null || emailId.trim().isEmpty()) {
                logger.warn("No authenticated user found in security context");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication Required");
                error.put("message", "User must be authenticated to retrieve roles");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            logger.info("Fetching roles for authenticated user: {}", emailId);
            
            UserRolesDTO userRoles = userRoleService.getUserRoles(emailId);
            
            logger.info("Successfully retrieved {} roles for user: {}", 
                       userRoles.getRoles().size(), emailId);
            
            return ResponseEntity.ok(userRoles);
            
        } catch (Exception e) {
            logger.error("Error retrieving user roles: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "Failed to retrieve user roles");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get roles for a specific user by email (admin/debug endpoint).
     * This endpoint can be used by admins or for debugging purposes.
     * 
     * @param emailId Email ID of the user to check
     * @return UserRolesDTO containing list of role strings
     */
    @GetMapping("/roles/{emailId}")
    public ResponseEntity<?> getUserRolesByEmail(@PathVariable String emailId) {
        try {
            logger.info("Fetching roles for user: {}", emailId);
            
            if (emailId == null || emailId.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid Request");
                error.put("message", "Email ID is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            UserRolesDTO userRoles = userRoleService.getUserRoles(emailId);
            
            logger.info("Successfully retrieved {} roles for user: {}", 
                       userRoles.getRoles().size(), emailId);
            
            return ResponseEntity.ok(userRoles);
            
        } catch (Exception e) {
            logger.error("Error retrieving roles for user {}: {}", emailId, e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "Failed to retrieve user roles");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Extract authenticated user's email from security context.
     * 
     * @return User's email ID or null if not authenticated
     */
    private String getAuthenticatedUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof String) {
                    return (String) principal;
                }
                
                if (principal != null) {
                    return authentication.getName();
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error extracting user email from security context: {}", e.getMessage(), e);
            return null;
        }
    }
}
