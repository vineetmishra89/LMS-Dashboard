package com.example.lms.dto;

public class AnalyticsSummaryDto {
  private long completedCount;
  private long enrolledCount;
  private double hoursLearned;

  public AnalyticsSummaryDto() {}
  public AnalyticsSummaryDto(long completedCount, long enrolledCount, double hoursLearned) {
    this.completedCount = completedCount;
    this.enrolledCount = enrolledCount;
    this.hoursLearned = hoursLearned;
  }
  public long getCompletedCount() { return completedCount; }
  public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
  public long getEnrolledCount() { return enrolledCount; }
  public void setEnrolledCount(long enrolledCount) { this.enrolledCount = enrolledCount; }
  public double getHoursLearned() { return hoursLearned; }
  public void setHoursLearned(double hoursLearned) { this.hoursLearned = hoursLearned; }
}
