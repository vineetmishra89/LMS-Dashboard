package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "LMS_VIDEO_PROGRESS")
@Setter
@Getter
public class VideoProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "TRNG_ID")
  private String courseId;

  @Column(name = "MODULE_ID")
  private String lessonId;

  @Column(name = "current_ts")
  private BigDecimal currentTime;

  @Column(name = "duration")
  private BigDecimal duration;

  @Column(name = "watch_time")
  private BigDecimal watchTime;

  @Column(name="COMPLETED_FLAG")
  private Boolean completed;

  @Column(name = "last_watched_at")
  private OffsetDateTime lastWatchedAt;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name="MODULE_PROGRESS_PERCENTAGE")
  private Long moduleProgressPercentage;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TRNG_ENRL_DTL_ID")
  @JsonBackReference
  private CourseDetail courseDetail;

}
