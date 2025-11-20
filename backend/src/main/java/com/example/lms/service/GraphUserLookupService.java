package com.example.lms.service;

import java.util.Optional;

public interface GraphUserLookupService {
    
    /**
     * Lookup user email by display name using Graph API
     * 
     * @param bearerToken Bearer token for Graph API authentication
     * @param displayName Display name to search for
     * @return Email address if found, empty if not found or error
     */
    Optional<String> lookupEmailByDisplayName(String bearerToken, String displayName);
}
