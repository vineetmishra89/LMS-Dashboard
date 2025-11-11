package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_EMPLOYEE_DTLS", schema = "lms_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetails {

  @Id
  @Column(name = "EMAIL_ID", nullable = false, length = 100)
  private String emailId;

  @Column(name = "EMP_ID", nullable = false)
  private Integer empId;

  @Column(name = "EMP_NAME", length = 200)
  private String empName;

  @Column(name = "PASSWRD", length = 255)
  private String passwrd;

  @Column(name = "EMP_DESIGNATION", length = 200)
  private String empDesignation;

  @Column(name = "PROJECT_NAME", length = 500)
  private String projectName;

  @Column(name = "RO_EMAIL_ID", length = 100)
  private String roEmailId;

  @Column(name = "EMP_ACTIVE_FLAG", length = 1)
  private String empActiveFlag;

  @Column(name = "CREATED_BY", length = 100)
  private String createdBy;

  @Column(name = "CREATED_TS")
  private OffsetDateTime createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  private OffsetDateTime updatedTs;
}
