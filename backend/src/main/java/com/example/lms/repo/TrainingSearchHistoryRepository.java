package com.example.lms.repo;

import com.example.lms.domain.TrainingSearchHistory;
import com.example.lms.domain.TrainingSearchHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingSearchHistoryRepository extends JpaRepository<TrainingSearchHistory, TrainingSearchHistoryId> {
}
