package com.example.lms.dto;

import com.example.lms.dto.TrainingNameDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterDto {

  private List<TrainingNameDto> trainingNameList;

  private List<String> levelList;

  private List<String> categoryList;

  private List<String> trainerNameList;

}
