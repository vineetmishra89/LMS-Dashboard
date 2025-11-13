package com.example.lms.controller;

import com.example.lms.dto.AuthRequest;
import com.example.lms.dto.ChangePasswordRequest;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
import com.example.lms.service.AuthService;
import com.example.lms.service.impl.GraphApiService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Handles login, logout, and password change requests.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    private GraphApiService graphApiService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates user and returns JWT token.
     *
     * @param loginRequest Login credentials (email and password)
     * @return Login response with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for user: {}", loginRequest.getEmailId());

        try {
            LoginResponse response = authService.authenticateUser(loginRequest);
            logger.info("Login successful for user: {}", loginRequest.getEmailId());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Login failed for user {}: {}", loginRequest.getEmailId(), e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication Failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during login for user {}: {}",
                    loginRequest.getEmailId(), e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An unexpected error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Changes user's password.
     * Requires authentication.
     *
     * @param changePasswordRequest Current and new password
     * @return Success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailId = authentication.getName();

            logger.info("Password change request received for user: {}", emailId);

            authService.changePassword(emailId, changePasswordRequest);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            logger.info("Password changed successfully for user: {}", emailId);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Password change failed: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Password Change Failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during password change: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An unexpected error occurred. Please try again later.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Logout endpoint (client-side token invalidation).
     * In stateless JWT authentication, logout is primarily handled on the client side
     * by removing the token. This endpoint is provided for consistency.
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String emailId = authentication.getName();
                logger.info("Logout request received for user: {}", emailId);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Logout Error");
            errorResponse.put("message", "An error occurred during logout");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

  @Operation(
    summary = "Validate Azure AD Token",
    description = "Validates an Azure AD access token and returns user information"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Token is valid",
      content = @Content(mediaType = "application/json")),
    @ApiResponse(responseCode = "401", description = "Invalid token",
      content = @Content(mediaType = "application/json")),
    @ApiResponse(responseCode = "500", description = "Internal server error",
      content = @Content(mediaType = "application/json"))
  })
  @PostMapping("/validate-token")
  public ResponseEntity<Map<String, Object>> validateToken(
    @Parameter(description = "Request containing Azure AD access token", required = true)
    @RequestBody AuthRequest authRequest) {
    Map<String, Object> response = new HashMap<>();

    try {
      boolean isValid = graphApiService.validateToken(authRequest.getAccessToken());
      response.put("valid", isValid);

      if (isValid) {
        // If token is valid, also get user info
        JsonNode userInfo = graphApiService.getUserInfo(authRequest.getAccessToken());
        response.put("userInfo", userInfo);
        return ResponseEntity.ok(response);
      } else {
        response.put("error", "Invalid access token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }
    } catch (Exception e) {
      response.put("error", "Token validation failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
