package com.example.lms.constants;

/**
 * Constants for application roles.
 * These roles are used for authorization and UI visibility control.
 */
public final class RoleConstants {
    
    /**
     * L&D Admin role - users who can manage trainings and assignments
     */
    public static final String ROLE_LND_ADMIN = "ROLE_LND_ADMIN";
    
    /**
     * Reporting Officer role - users who have employees reporting to them
     */
    public static final String ROLE_RO = "ROLE_RO";
    
    /**
     * Project Manager role - users who are PM on projects
     */
    public static final String ROLE_PROJECT_MANAGER = "ROLE_PROJECT_MANAGER";
    
    /**
     * Project Admin role - users who are ADM on projects
     */
    public static final String ROLE_PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";
    
    /**
     * Offshore Delivery Director role - users who are Offshore DD on projects
     */
    public static final String ROLE_OFFSHORE_DD = "ROLE_OFFSHORE_DD";
    
    /**
     * Onsite Delivery Director role - users who are Onsite DD on projects
     */
    public static final String ROLE_ONSITE_DD = "ROLE_ONSITE_DD";
    
    /**
     * HR Business Partner role - users who are HRBP on projects
     */
    public static final String ROLE_HRBP = "ROLE_HRBP";
    
    /**
     * Trainer role - users who conduct trainings
     */
    public static final String ROLE_TRAINER = "ROLE_TRAINER";
    
    /**
     * Trainee role - all employees in LMS_EMPLOYEE_DTLS
     */
    public static final String ROLE_TRAINEE = "ROLE_TRAINEE";
    
    private RoleConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }
}
