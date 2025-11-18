package com.example.lms.service;

import com.example.lms.dto.ProjectExcelSyncResult;

public interface ProjectExcelSyncService {
    
    /**
     * Sync project data from Excel file to database
     * 
     * @param bearerToken Bearer token for Graph API authentication
     * @return Sync result with counts and errors
     */
    ProjectExcelSyncResult syncFromExcel(String bearerToken);
}
