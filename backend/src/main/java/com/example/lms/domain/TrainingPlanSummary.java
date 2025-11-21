package com.example.lms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_TRNG_PLAN_SUMMARY", schema = "lms_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlanSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRNG_PLAN_ID", nullable = false)
    private Long trngPlanId;

    @Column(name = "TRNG_PLAN_NAME", nullable = false, length = 500)
    private String trngPlanName;

    @Column(name = "EMAIL_ID", nullable = false, length = 100)
    private String emailId;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "CREATED_TS")
    private OffsetDateTime createdTs;

    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;

    @Column(name = "UPDATED_TS")
    private OffsetDateTime updatedTs;
}
