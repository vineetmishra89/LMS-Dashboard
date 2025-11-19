package com.example.lms.service.impl;

import com.example.lms.service.GraphUserLookupService;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GraphUserLookupServiceImpl implements GraphUserLookupService {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphUserLookupServiceImpl.class);
    
    @Autowired
    private GraphClientProvider graphClientProvider;
    
    private final Map<String, Optional<String>> lookupCache = new ConcurrentHashMap<>();
    
    @Override
    public Optional<String> lookupEmailByDisplayName(String bearerToken, String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            logger.warn("Display name is null or empty");
            return Optional.empty();
        }
        
        String normalizedName = displayName.trim();
        
        if (lookupCache.containsKey(normalizedName)) {
            logger.debug("Returning cached result for display name: {}", normalizedName);
            return lookupCache.get(normalizedName);
        }
        
        try {
            GraphServiceClient<Request> client = graphClientProvider.getGraphClientWithBearerToken(bearerToken);
            
            String[] names = normalizedName.split("[/&]");
            for (String name : names) {
                String trimmedName = name.trim();
                if (trimmedName.isEmpty()) {
                    continue;
                }
                
                Optional<String> email = lookupSingleName(client, trimmedName);
                if (email.isPresent()) {
                    logger.info("Successfully resolved display name '{}' to email: {}", trimmedName, email.get());
                    lookupCache.put(normalizedName, email);
                    return email;
                }
            }
            
            logger.warn("Could not find email for display name: {}", normalizedName);
            lookupCache.put(normalizedName, Optional.empty());
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error looking up email for display name '{}': {}", normalizedName, e.getMessage(), e);
            lookupCache.put(normalizedName, Optional.empty());
            return Optional.empty();
        }
    }
    
    private Optional<String> lookupSingleName(GraphServiceClient<Request> client, String displayName) {
        try {
            logger.debug("Trying exact match for display name: {}", displayName);
            UserCollectionPage users = client.users()
                    .buildRequest()
                    .filter(String.format("displayName eq '%s'", escapeSingleQuotes(displayName)))
                    .select("displayName,mail,userPrincipalName,accountEnabled")
                    .get();
            
            if (users != null && users.getCurrentPage() != null && !users.getCurrentPage().isEmpty()) {
                return extractEmailFromUsers(users.getCurrentPage(), displayName);
            }
            
            logger.debug("Trying startsWith match for display name: {}", displayName);
            users = client.users()
                    .buildRequest()
                    .filter(String.format("startswith(displayName,'%s')", escapeSingleQuotes(displayName)))
                    .select("displayName,mail,userPrincipalName,accountEnabled")
                    .get();
            
            if (users != null && users.getCurrentPage() != null && !users.getCurrentPage().isEmpty()) {
                return extractEmailFromUsers(users.getCurrentPage(), displayName);
            }
            
            logger.debug("No users found for display name: {}", displayName);
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error in Graph API lookup for display name '{}': {}", displayName, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    private Optional<String> extractEmailFromUsers(List<User> users, String searchedName) {
        List<User> enabledUsers = users.stream()
                .filter(u -> u.accountEnabled != null && u.accountEnabled)
                .toList();
        
        List<User> usersToCheck = enabledUsers.isEmpty() ? users : enabledUsers;
        
        if (usersToCheck.isEmpty()) {
            return Optional.empty();
        }
        
        if (usersToCheck.size() > 1) {
            logger.warn("Multiple users found for display name '{}'. Using first match.", searchedName);
        }
        
        User user = usersToCheck.get(0);
        
        if (user.mail != null && !user.mail.trim().isEmpty()) {
            return Optional.of(user.mail);
        } else if (user.userPrincipalName != null && !user.userPrincipalName.trim().isEmpty()) {
            return Optional.of(user.userPrincipalName);
        }
        
        logger.warn("User found for display name '{}' but has no email", searchedName);
        return Optional.empty();
    }
    
    private String escapeSingleQuotes(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }
    
    public void clearCache() {
        lookupCache.clear();
        logger.info("Lookup cache cleared");
    }
}
