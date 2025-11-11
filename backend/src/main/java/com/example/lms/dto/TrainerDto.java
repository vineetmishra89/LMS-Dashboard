package com.example.lms.dto;

import java.util.Objects;

public class TrainerDto {
    private String trainerName;
    private String emailId;

    public TrainerDto(String trainerName, String emailId) {
      this.trainerName = trainerName;
      this.emailId = emailId;
    }

    public String getTrainerName() {
      return trainerName;
    }

    public String getEmailId() {
      return emailId;
    }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrainerDto that = (TrainerDto) o;
    return Objects.equals(trainerName, that.trainerName) &&
      Objects.equals(emailId, that.emailId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trainerName, emailId);
  }
}
