package com.example.lms.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ROPMDashboardEmployeesRequest {
    private String roEmailId;
    private List<String> sbus;
    private List<String> projects;
}
