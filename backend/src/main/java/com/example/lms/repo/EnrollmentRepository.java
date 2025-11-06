package com.example.lms.repo;

import com.example.lms.domain.EnrollmentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentMapping, Long> {
  List<EnrollmentMapping> findByUserId(String userId);
  List<EnrollmentMapping> findByUserIdAndStatus(String userId, String status);

  @Query("Select count(e) from EnrollmentMapping e where e.userId = :userId and e.status=:status")
  long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query(value="delete from lms_schema.LMS_USER_TRNG_ENROLLMENT_MAPPING where trng_id= :trainingId and email_id=:userId", nativeQuery = true)
  void deleteNativeByUserIdAndTrainingId(@Param("userId") String userId,@Param("trainingId") Long trainingId);

}
