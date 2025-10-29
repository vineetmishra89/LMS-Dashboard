package com.example.lms.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@Getter
@Setter
public class CourseCardDetailDto {

  private long trngId;

  private String courseName;

  private String trainerNames;

  private String trainerEmailIds;

  private long duration;

  private String level;

  private long modules;

  private String rating;

  private String category;

  public CourseCardDetailDto(long trngId, String courseName,
                             String trainerNames, String trainerEmailIds, long duration, String level, long modules, String rating, String category) {
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
