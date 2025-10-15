package com.example.lms.dto;

import lombok.Getter;

@Getter
public enum TimePeriod {
  LAST_24_HOURS("Last 24 Hours", 1),
  LAST_WEEK("Last 1 Week", 7),
  LAST_2_MONTHS("Last 2 Months", 60),
  LAST_6_MONTHS("Last 6 Months", 180),
  LAST_YEAR("Last 1 Year", 365);

  private final String label;
  private final int days;

  TimePeriod(String label, int days) {
    this.label = label;
    this.days = days;
  }
}
