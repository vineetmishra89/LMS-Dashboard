package com.example.lms.constant;

public enum EnrollmentStatus {
  ENROLLED("Enrolled"), PENDING("Pending"), COMPLETED("Completed");

  private EnrollmentStatus(String status){
    this.status = status;
  }

  private String status;

  public String getStatus(){
    return this.status;
  }
}
