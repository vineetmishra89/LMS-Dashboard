package com.example.lms.repo;

import com.example.lms.domain.FeedbackDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository  extends JpaRepository<FeedbackDetails, Long> {

    @Query(value = "SELECT  * FROM lms_schema.LMS_USER_TRNG_FEEDBACK trngF " +
    "WHERE trngF.trng_enrl_id = :enrollmentId", nativeQuery = true)
    java.util.Optional<FeedbackDetails> findEnrollmentId(Long enrollmentId);

}
