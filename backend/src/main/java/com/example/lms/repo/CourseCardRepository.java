package com.example.lms.repo;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.CourseCardDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseCardRepository extends JpaRepository<CourseDetail, String> {
  // Use the entity graph so details are fetched with masters
  @Query(value = "select ts.TRNG_ID, ts.TRNG_TOPIC Course_Name ,STRING_AGG(td.TRAINER_NAME::text, ',') Trainer_Names, SUM(tdt.Module_duration) Duration,ts.LEVEL_CODE Level, count(tdt.Module_id) Modules \n" +
    "    from lms_schema.LMS_TRNG_SUMMARY ts, lms_schema.LMS_TRAINER_DTLS td, lms_schema.LMS_TRNG_DTLS tdt, lms_schema.LMS_USER_TRNG_ENROLLMENT_MAPPING tem\n" +
    "    where ts.TRNG_ID = tdt.TRNG_ID and tdt.TRAINER_ID = td.TRAINER_ID and ts.TRNG_ID = tem.TRNG_ID\n" +
    "    AND tem.ENROLLED_TS BETWEEN (NOW()-INTERVAL '24 month') AND NOW() and tem.status ='Enrolled'\n" +
    "    group by ts.TRNG_ID ORDER BY COUNT(tem.TRNG_ID) DESC", nativeQuery = true)
  List<Object[]> findCourseCardDetailsByEnrollment();

  /*@Query(value = "select ts.TRNG_TOPIC Course_Name ,STRING_AGG(td.TRAINER_NAME::text, ',') Trainer_Names, SUM(tdt.Module_duration) Duration,ts.LEVEL_CODE Level, count(tdt.Module_id) Modules\n" +
    "    from LMS_TRNG_SUMMARY ts, LMS_TRAINER_DTLS td, LMS_TRNG_DTLS tdt, LMS_TRNG_SEARCH_HIST tsh\n" +
    "    where ts.TRNG_ID = tdt.TRNG_ID and tdt.TRAINER_ID = td.TRAINER_ID and ts.TRNG_ID = tsh.TRNG_ID\n" +
    "   AND tsh.VIEW_TS BETWEEN (NOW()-INTERVAL '1 month') AND NOW()\n" +
    "    group by ts.TRNG_ID ORDER BY count(ts.TRNG_ID) DESC",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByView();

  @Query(value = "select ts.TRNG_TOPIC,td.TRAINER_NAME, ts.LEVEL_CODE, SUM(tdt.MODULE_DURATION) Duration,\n" +
    "Count(Module_id) TotalModule, count(ts.TRNG_ID) totalTrng from LMS_TRNG_SUMMARY ts, LMS_TRAINER_DTLS td, LMS_TRAINING_DTLS tdt\n" +
    "where ts.TRNG_ID = tdt.TRNG_ID and tdt.TRAINER_ID = td.TRAINER_ID and ts.TRNG_ID = tsh.TRNG_ID\n" +
    "and ts.CREATED_TS >= date_trunc('month', NOW()) - INTERVAL '1 month'\n" +
    "  AND ts.CREATED_TS < date_trunc('month', NOW()) order by ts.RATING desc",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByTopRate();

  @Query(value = "select ts.TRNG_TOPIC,td.TRAINER_NAME, ts.LEVEL_CODE, SUM(tdt.MODULE_DURATION) Duration,\n" +
    "Count(Module_id) TotalModule, count(ts.TRNG_ID) totalTrng from LMS_TRNG_SUMMARY ts, LMS_TRAINER_DTLS td, LMS_TRAINING_DTLS tdt\n" +
    "where ts.TRNG_ID = tdt.TRNG_ID and tdt.TRAINER_ID = td.TRAINER_ID and ts.TRNG_ID = tsh.TRNG_ID\n" +
    "and ts.CREATED_TS >= date_trunc('month', NOW()) - INTERVAL '1 month'\n" +
    "  AND ts.CREATED_TS < date_trunc('month', NOW()) and ts.Category=:categoryType",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByCourseCategory(String categoryType);*/
}
