package com.example.lms.repo;

import com.example.lms.domain.LearningHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningHoursRepository extends JpaRepository<LearningHours, Long> {
  List<LearningHours> findByUserId(String userId);
}
