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

  private long duration;

  private String level;

  private long modules;

  public CourseCardDetailDto(long trngId, String courseName, String trainerNames, long duration, String level, long modules) {
    this.trngId = trngId;
    this.courseName = courseName;
    this.trainerNames = trainerNames;
    this.duration = duration;
    this.level = level;
    this.modules= modules;
  }
}
