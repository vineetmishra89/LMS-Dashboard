package com.example.lms.service.impl;

import com.example.lms.domain.EmployeeDetails;
import com.example.lms.dto.ChangePasswordRequest;
import com.example.lms.dto.LoginRequest;
import com.example.lms.dto.LoginResponse;
import com.example.lms.repo.EmployeeDetailsRepository;
import com.example.lms.service.AuthService;
import com.example.lms.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Implementation of AuthService for handling authentication operations.
 * Phase 1: Uses plain text password comparison.
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthServiceImpl(EmployeeDetailsRepository employeeDetailsRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    @Transactional(readOnly = true)
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authentication attempt for user: {}", loginRequest.getEmailId());
        
        try {
            Optional<EmployeeDetails> employeeOpt = employeeDetailsRepository
                    .findByEmailIdIgnoreCase(loginRequest.getEmailId());
            
            if (employeeOpt.isEmpty()) {
                logger.warn("Authentication failed: User not found - {}", loginRequest.getEmailId());
                throw new RuntimeException("Invalid email or password");
            }
            
            EmployeeDetails employee = employeeOpt.get();
            
            if (!"Y".equalsIgnoreCase(employee.getEmpActiveFlag())) {
                logger.warn("Authentication failed: Inactive account - {}", loginRequest.getEmailId());
                throw new RuntimeException("Account is inactive. Please contact administrator.");
            }
            
            if (employee.getPasswrd() == null || !employee.getPasswrd().equals(loginRequest.getPassword())) {
                logger.warn("Authentication failed: Invalid password for user - {}", loginRequest.getEmailId());
                throw new RuntimeException("Invalid email or password");
            }
            
            String role = determineUserRole(employee);
            
            String token = jwtTokenProvider.generateToken(
                    employee.getEmailId(),
                    employee.getEmpName(),
                    role
            );
            
            OffsetDateTime expiresAt = jwtTokenProvider.getExpirationFromToken(token);
            
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresAt(expiresAt)
                    .user(LoginResponse.UserInfo.builder()
                            .emailId(employee.getEmailId())
                            .name(employee.getEmpName())
                            .designation(employee.getEmpDesignation())
                            .role(role)
                            .build())
                    .build();
            
            logger.info("Authentication successful for user: {}", loginRequest.getEmailId());
            return response;
            
        } catch (RuntimeException e) {
            logger.error("Authentication error for user {}: {}", loginRequest.getEmailId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user {}: {}", 
                    loginRequest.getEmailId(), e.getMessage(), e);
            throw new RuntimeException("Authentication failed due to system error", e);
        }
    }
    
    @Override
    @Transactional
    public void changePassword(String emailId, ChangePasswordRequest changePasswordRequest) {
        logger.info("Password change request for user: {}", emailId);
        
        try {
            Optional<EmployeeDetails> employeeOpt = employeeDetailsRepository
                    .findByEmailIdIgnoreCase(emailId);
            
            if (employeeOpt.isEmpty()) {
                logger.error("Password change failed: User not found - {}", emailId);
                throw new RuntimeException("User not found");
            }
            
            EmployeeDetails employee = employeeOpt.get();
            
            if (employee.getPasswrd() == null || 
                !employee.getPasswrd().equals(changePasswordRequest.getCurrentPassword())) {
                logger.warn("Password change failed: Invalid current password for user - {}", emailId);
                throw new RuntimeException("Current password is incorrect");
            }
            
            if (changePasswordRequest.getCurrentPassword().equals(changePasswordRequest.getNewPassword())) {
                logger.warn("Password change failed: New password same as current for user - {}", emailId);
                throw new RuntimeException("New password must be different from current password");
            }
            
            employee.setPasswrd(changePasswordRequest.getNewPassword());
            employee.setUpdatedBy(emailId);
            employee.setUpdatedTs(OffsetDateTime.now());
            
            employeeDetailsRepository.save(employee);
            
            logger.info("Password changed successfully for user: {}", emailId);
            
        } catch (RuntimeException e) {
            logger.error("Password change error for user {}: {}", emailId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during password change for user {}: {}", 
                    emailId, e.getMessage(), e);
            throw new RuntimeException("Password change failed due to system error", e);
        }
    }
    
    /**
     * Determines user role based on employee details.
     * Logic: If user has direct reports (is an RO), assign RO role, otherwise TRAINEE.
     * 
     * @param employee Employee details
     * @return User role
     */
    private String determineUserRole(EmployeeDetails employee) {
        boolean isRo = employeeDetailsRepository.existsByEmailIdIgnoreCase(employee.getEmailId()) &&
                       employee.getRoEmailId() != null;
        
        return isRo ? "RO" : "TRAINEE";
    }
}
