package com.example.lms.repo;

import com.example.lms.domain.EnrollmentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EnrollmentDetailsRepository extends JpaRepository<EnrollmentDetails, Long> {

  @Query(value="select count(1) from lms_schema.LMS_USER_TRNG_ENROLLMENT_MAPPING map, lms_schema.LMS_USER_TRNG_ENROLLMENT_DTLS dtls where map.trng_enrl_id=dtls.trng_enrl_id and map.email_id=:userId and dtls.status='Enrolled' and map.trng_id=:trainingId", nativeQuery = true)
  int countEnrollmentByStatusAndEnrollmentId(@Param("trainingId") Integer trainingId, @Param("userId")String userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query(value="delete from lms_schema.LMS_USER_TRNG_ENROLLMENT_DTLS where trng_enrl_id= :enrollmentId", nativeQuery = true)
  void deleteEnrollDetailByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query(value="update lms_schema.LMS_USER_TRNG_ENROLLMENT_DTLS set status='COMPLETED', updated_ts=CURRENT_TIMESTAMP where trng_enrl_dtl_id= :enrollmentDetailsId", nativeQuery = true)
  void updateEnrollmentStatusByEnrollmentDtlId(@Param("enrollmentDetailsId") Long enrollmentDetailsId);

}
