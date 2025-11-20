package com.example.lms.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name = "lms_video_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lms_video_progress_id_generator")
  @SequenceGenerator(name = "lms_video_progress_id_generator", sequenceName = "lms_schema.video_progress_id_seq", allocationSize = 1)
  @Column(name = "VIDEO_PROGRESS_ID")
  private Long id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "TRNG_ENRL_DTL_ID")
  private Long trainingEnrollmentDtlId;

  @Column(name = "TRNG_ID")
  private Integer courseId;

  @Column(name = "module_id")
  private Integer lessonId;

  @Column(name = "module_progress_percentage")
  private Long progressPercent;

  @Column(name = "current_ts")
  private BigDecimal currentTime;

  @Column(name = "duration")
  private BigDecimal duration;

  @Column(name = "watch_time")
  private BigDecimal watchTime;

  @Column(name = "COMPLETED_FLAG")
  private Boolean completed;

  @Column(name = "last_watched_at")
  private OffsetDateTime lastWatchedAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;
}
