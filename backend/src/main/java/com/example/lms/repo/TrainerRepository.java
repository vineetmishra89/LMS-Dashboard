package com.example.lms.repo;

import com.example.lms.domain.LMSTrainerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainerRepository extends JpaRepository<LMSTrainerDetails, Long> {

  @Query(value = "select distinct email_id from lms_schema.lms_trainer_dtls order by email_id", nativeQuery = true)
  List<Object> getAllTrainers();


}
