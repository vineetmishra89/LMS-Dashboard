package com.example.lms.dto;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.LMSTrainerDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {

  private Long trainingId;

  private String trainingName;

  private String category;

  private String level;

  private Integer duration;

  private String trainingDesc;

  private String trainerName;

  private Integer rating;

  private List<CourseDetail> courseDetailList;

  private Set<TrainerDto> trainerDetailList;

  public void addTrainerDto(TrainerDto trainerDto){
    if(null == this.trainerDetailList){
      this.trainerDetailList = new HashSet<>();
      trainerDetailList.add(trainerDto);
    }
  }
  public void addTrainerDtoList(Set<TrainerDto> trainerDtoSet) {
    if (null == this.trainerDetailList) {
      this.trainerDetailList = new HashSet<>();
      trainerDetailList.addAll(trainerDtoSet);
    }
  }
}
