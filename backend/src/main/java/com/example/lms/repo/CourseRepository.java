package com.example.lms.repo;

import com.example.lms.domain.TrainingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CourseRepository extends JpaRepository<TrainingDetails, String>, JpaSpecificationExecutor<TrainingDetails> {
  List<TrainingDetails> findByCategoryIgnoreCase(String category);
  List<TrainingDetails> findByInstructorNameIgnoreCase(String instructorName);
}
