package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_PROJECT_DTLS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetails {

  @Id
  @Column(name = "PROJECT_NAME")
  private String projectName;

  @Column(name = "PM_EMAIL_ID")
  private String pmEmailId;

  @Column(name = "ADM_EMAIL_ID")
  private String admEmailId;

  @Column(name = "OFFSHORE_DD_EMAIL_ID")
  private String offshoreDdEmailId;

  @Column(name = "ONSITE_DD_EMAIL_ID")
  private String onsiteDdEmailId;

  @Column(name = "HRBP_EMAIL_ID")
  private String hrbpEmailId;

  @Column(name = "PROJ_ACTIVE_FLAG")
  private String projActiveFlag;

  @Column(name = "created_ts")
  private OffsetDateTime createdTs;

  @Column(name = "updated_ts")
  private OffsetDateTime updatedTs;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_By")
  private String updatedBy;

}
