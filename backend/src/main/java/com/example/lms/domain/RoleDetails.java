package com.example.lms.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entity class for LMS_ROLE_DETAILS table.
 * Represents role definitions in the system.
 */
@Entity
@Table(name = "LMS_ROLE_DETAILS", schema = "lms_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetails {
    
    @Id
    @Column(name = "ROLE_ID", nullable = false)
    private Integer roleId;
    
    @Column(name = "ROLE_NAME", nullable = false, length = 100)
    private String roleName;
    
    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;
    
    @Column(name = "CREATED_TS")
    private OffsetDateTime createdTs;
    
    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;
    
    @Column(name = "UPDATED_TS")
    private OffsetDateTime updatedTs;
}
