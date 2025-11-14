package com.example.lms.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entity class for LMS_USER_ROLES table.
 * Maps users to their roles.
 */
@Entity
@Table(name = "LMS_USER_ROLES", schema = "lms_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    
    @Id
    @Column(name = "EMAIL_ID", nullable = false, length = 100)
    private String emailId;
    
    @Column(name = "ROLE_ID", nullable = false)
    private Integer roleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID", insertable = false, updatable = false)
    private RoleDetails roleDetails;
    
    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;
    
    @Column(name = "CREATED_TS")
    private OffsetDateTime createdTs;
    
    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;
    
    @Column(name = "UPDATED_TS")
    private OffsetDateTime updatedTs;
}
