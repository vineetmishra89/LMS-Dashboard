package com.example.lms.dto;

import com.example.lms.domain.CourseDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {

  private String trainingName;

  private String category;

  private String level;

  private Integer duration;

  private String trainingDesc;

  private String trainerName;

  private String rating;

  private List<CourseDetail> courseDetailList;
}
