package com.example.lms.service;

import com.example.lms.dto.ChangePasswordRequest;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {
    
    /**
     * Authenticates a user with email and password.
     * 
     * @param loginRequest Login credentials
     * @return Login response with JWT token and user info
     * @throws RuntimeException if authentication fails
     */
    LoginResponse authenticateUser(LoginRequest loginRequest);
    
    /**
     * Changes user's password.
     * 
     * @param emailId User's email ID
     * @param changePasswordRequest Current and new password
     * @throws RuntimeException if password change fails
     */
    void changePassword(String emailId, ChangePasswordRequest changePasswordRequest);
}
