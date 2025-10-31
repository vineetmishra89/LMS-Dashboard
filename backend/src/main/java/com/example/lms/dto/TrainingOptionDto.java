package com.example.lms.dto;

/**
 * DTO for training options in assignment dropdowns.
 * Contains minimal information needed for RO Dashboard training selection.
 */
public record TrainingOptionDto(
  Long trainingId,
  String trainingName,
  String category,
  String level
) {}
