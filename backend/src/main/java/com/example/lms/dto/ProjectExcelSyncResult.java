package com.example.lms.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectExcelSyncResult {
    private String excelFilePath;
    private int totalRows;
    private int projectsInserted;
    private int projectsSkippedExisting;
    private int employeesUpdated;
    private int graphLookupsSucceeded;
    private int graphLookupsFailed;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
