package com.example.lms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EnrollmentDetailsId implements Serializable {

  @Column(name="trng_enrl_id")
  private Long trainingEmrollmentId;

  @Column(name="module_id")
  private Long moduleId;

}
