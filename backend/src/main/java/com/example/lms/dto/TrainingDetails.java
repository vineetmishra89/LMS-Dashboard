package com.example.lms.dto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20000)
    private String Training_Name;
    @Column(length = 20000)
    private String description;
    @Column(length = 20000)
    private String content;
    @Column(length = 20000)
    private String duration;
    private String trainers_current_feedback;
    @Column(length = 20000)
    private String current_user_feedback;
    @Column(length = 20000)
    private String review_comments;
    @Column(length = 20000)
    private String trainer_Name;
    @Column(length = 20000)
    private String prerequisite;
    @Column(length = 20000)
    private String level;
    @Column(length = 20000)
    private String tools_needed;
    @Column(length = 90000)
    private String recording_link;

}
