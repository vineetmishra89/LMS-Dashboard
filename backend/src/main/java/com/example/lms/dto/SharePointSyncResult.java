package com.example.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for SharePoint sync operation results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharePointSyncResult {
    
    private boolean dryRun;
    private int foldersProcessed;
    private int coursesMatched;
    private int coursesUpdated;
    private int modulesInserted;
    private int modulesUpdated;
    private int modulesSkipped;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }
    
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }
}
