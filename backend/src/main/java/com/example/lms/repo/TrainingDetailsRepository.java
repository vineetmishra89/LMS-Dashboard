package com.example.lms.repo;

import com.example.lms.dto.TrainingDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingDetailsRepository extends JpaRepository<TrainingDetails, Long> {
}
