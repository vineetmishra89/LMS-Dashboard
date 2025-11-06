package com.example.lms.service;

import com.example.lms.dto.EmployeeSyncResult;

public interface EmployeeGraphSyncService {
    
    EmployeeSyncResult syncEmployeeHierarchyFromGraph(String rootEmailId);
}
