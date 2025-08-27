package com.example.lms.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "learning_hours")
public class LearningHours {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String userId;
  private String category;
  private double totalHours;
  private double monthHours;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public double getTotalHours() { return totalHours; }
  public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
  public double getMonthHours() { return monthHours; }
  public void setMonthHours(double monthHours) { this.monthHours = monthHours; }
}
