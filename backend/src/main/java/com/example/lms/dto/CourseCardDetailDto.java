package com.example.lms.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
@Getter
@Setter
public class CourseCardDetailDto {

  private Long trngId;

  private String courseName;

  private String trainerNames;

  private String trainerEmailIds;

  private Long duration;

  private String level;

  private Long modules;

  private BigDecimal rating;

  private String category;

  public CourseCardDetailDto(long trngId, String courseName,
                             String trainerNames, String trainerEmailIds, long duration, String level, long modules, BigDecimal rating, String category) {
    this.trngId = trngId;
    this.courseName = courseName;
    this.trainerNames = trainerNames;
    this.trainerEmailIds = trainerEmailIds;
    this.duration = duration;
    this.level = level;
    this.modules= modules;
    this.rating = rating;
    this.category = category;
  }
}
