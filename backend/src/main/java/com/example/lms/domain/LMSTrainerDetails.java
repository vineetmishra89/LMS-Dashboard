package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "LMS_TRAINER_DTLS")
public class LMSTrainerDetails {

  @Id
  @Column(name = "trainer_id")
  private Long trainerid;

  @Column(name = "trainer_emp_id")
  private Long trainerEmpId;

  @Column(name = "trainer_name")
  private String trainerName;

  @Column(name = "email_id")
  private String emailid;

  @Column(name = "trainer_type")
  private String trainerType;

}
