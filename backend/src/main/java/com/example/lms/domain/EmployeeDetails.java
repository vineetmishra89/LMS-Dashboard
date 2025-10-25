package com.example.lms.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "LMS_EMPLOYEE_DTLS", schema = "lms_schema")
public class EmployeeDetails {

  @Id
  @Column(name = "EMAIL_ID", nullable = false, length = 100)
  private String emailId;

  @Column(name = "EMP_ID", nullable = false)
  private Integer empId;

  @Column(name = "EMP_NAME", length = 200)
  private String empName;

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
  private Timestamp createdTs;

  @Column(name = "UPDATED_BY", length = 100)
  private String updatedBy;

  @Column(name = "UPDATED_TS")
  private Timestamp updatedTs;

  public EmployeeDetails() {
  }

  public String getEmailId() {
    return emailId;
  }

  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  public Integer getEmpId() {
    return empId;
  }

  public void setEmpId(Integer empId) {
    this.empId = empId;
  }

  public String getEmpName() {
    return empName;
  }

  public void setEmpName(String empName) {
    this.empName = empName;
  }

  public String getEmpDesignation() {
    return empDesignation;
  }

  public void setEmpDesignation(String empDesignation) {
    this.empDesignation = empDesignation;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getRoEmailId() {
    return roEmailId;
  }

  public void setRoEmailId(String roEmailId) {
    this.roEmailId = roEmailId;
  }

  public String getEmpActiveFlag() {
    return empActiveFlag;
  }

  public void setEmpActiveFlag(String empActiveFlag) {
    this.empActiveFlag = empActiveFlag;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Timestamp getCreatedTs() {
    return createdTs;
  }

  public void setCreatedTs(Timestamp createdTs) {
    this.createdTs = createdTs;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Timestamp getUpdatedTs() {
    return updatedTs;
  }

  public void setUpdatedTs(Timestamp updatedTs) {
    this.updatedTs = updatedTs;
  }
}
