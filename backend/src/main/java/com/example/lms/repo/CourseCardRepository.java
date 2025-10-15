package com.example.lms.repo;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.CourseCardDetailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface CourseCardRepository extends JpaRepository<CourseDetail, String> {

  @Query(value = "select ts.TRNG_ID, ts.TRNG_TOPIC Course_Name ,STRING_AGG(td.TRAINER_NAME::text, ',') Trainer_Names, SUM(tdt.Module_duration) Duration,ts.LEVEL_CODE Level, count(tdt.Module_id) Modules \n" +
    "    from lms_schema.LMS_TRNG_SUMMARY ts, lms_schema.LMS_TRAINER_DTLS td, lms_schema.LMS_TRNG_DTLS tdt, lms_schema.LMS_USER_TRNG_ENROLLMENT_MAPPING tem\n" +
    "    where ts.TRNG_ID = tdt.TRNG_ID and tdt.TRAINER_ID = td.TRAINER_ID and ts.TRNG_ID = tem.TRNG_ID\n" +
    "    AND tem.ENROLLED_TS BETWEEN (NOW()-CAST(:courseInterval AS INTERVAL)) AND NOW() and tem.status ='Enrolled'\n" +
    "    group by ts.TRNG_ID ORDER BY COUNT(tem.TRNG_ID) DESC", nativeQuery = true)
  List<Object[]> findCourseCardDetailsByEnrollment(String courseInterval);

  @Query(value = "WITH trng_details AS (\n" +
    "    SELECT ts.TRNG_ID, SUM(tdt.Module_duration) AS Duration, COUNT(tdt.module_id) AS Modules\n" +
    "    FROM lms_schema.LMS_TRNG_SUMMARY ts\n" +
    "    JOIN lms_schema.LMS_TRNG_DTLS tdt ON ts.TRNG_ID = tdt.TRNG_ID\n" +
    "    GROUP BY ts.TRNG_ID\n" +
    "    ORDER BY COUNT(ts.TRNG_ID) DESC\n" +
    "),\n" +
    "trngSummary AS (\n" +
    "    SELECT ts.TRNG_ID, ts.TRNG_TOPIC AS Course_Name, STRING_AGG(td.TRAINER_NAME::text, ',') AS Trainer_Names, ts.LEVEL_CODE AS Level\n" +
    "    FROM lms_schema.LMS_TRNG_SUMMARY ts\n" +
    "    JOIN lms_schema.LMS_TRNG_DTLS tdt ON ts.TRNG_ID = tdt.TRNG_ID\n" +
    "    JOIN lms_schema.LMS_TRAINER_DTLS td ON tdt.TRAINER_ID = td.TRAINER_ID\n" +
    "    JOIN lms_schema.LMS_TRNG_SEARCH_HIST tsh ON ts.TRNG_ID = tsh.TRNG_ID AND tdt.TRNG_ID = tsh.TRNG_ID\n" +
    "    WHERE tsh.VIEW_TS BETWEEN (NOW() - CAST(:courseInterval AS INTERVAL)) AND NOW()\n" +
    "    GROUP BY ts.TRNG_ID\n" +
    "    ORDER BY COUNT(ts.TRNG_ID) DESC\n" +
    ")\n" +
    "SELECT ts.TRNG_ID, Course_Name, Trainer_Names, duration, Level, modules\n" +
    "FROM trngSummary ts\n" +
    "LEFT JOIN trng_details td ON ts.TRNG_ID = td.TRNG_ID",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByView(String courseInterval);

  @Query(value = "SELECT\n" +
    "    ts.TRNG_ID,\n" +
    "    ts.TRNG_TOPIC AS Course_Name,\n" +
    "    STRING_AGG(td.TRAINER_NAME, ',') AS Trainer_Names,\n" +
    "    SUM(tdt.MODULE_DURATION) AS duration,\n" +
    "    ts.LEVEL_CODE AS Level,\n" +
    "    COUNT(tdt.MODULE_ID) AS Modules\n" +
    "FROM\n" +
    "    lms_schema.LMS_TRNG_SUMMARY ts\n" +
    "    JOIN lms_schema.LMS_TRNG_DTLS tdt ON ts.TRNG_ID = tdt.TRNG_ID\n" +
    "    JOIN lms_schema.LMS_TRAINER_DTLS td ON tdt.TRAINER_ID = td.TRAINER_ID\n" +
    " WHERE ts.CREATED_TS BETWEEN (NOW() - CAST(:courseInterval AS INTERVAL)) AND NOW()\n" +
    "GROUP BY\n" +
    "    ts.TRNG_ID, ts.TRNG_TOPIC, ts.LEVEL_CODE\n" +
    "ORDER BY\n" +
    "    ts.RATING DESC",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByTopRate(String courseInterval);

  @Query(value = "SELECT\n" +
    "    ts.TRNG_ID,\n" +
    "    ts.TRNG_TOPIC AS Course_Name,\n" +
    "    STRING_AGG(td.TRAINER_NAME, ',') AS Trainer_Names,\n" +
    "    SUM(tdt.MODULE_DURATION) AS duration,\n" +
    "    ts.LEVEL_CODE AS Level,\n" +
    "    COUNT(tdt.MODULE_ID) AS Modules\n" +
    "FROM\n" +
    "    lms_schema.LMS_TRNG_SUMMARY ts\n" +
    "    JOIN lms_schema.LMS_TRNG_DTLS tdt ON ts.TRNG_ID = tdt.TRNG_ID\n" +
    "    JOIN lms_schema.LMS_TRAINER_DTLS td ON tdt.TRAINER_ID = td.TRAINER_ID\n" +
    "WHERE ts.CREATED_TS BETWEEN (NOW() - CAST(:courseInterval AS INTERVAL)) AND NOW() and ts.Category=:categoryType\n" +
    "GROUP BY ts.TRNG_ID",
    nativeQuery = true)
  List<Object[]> findCourceCardDetailsByCourseCategory(@Param("categoryType") String categoryType, @Param("courseInterval") String courseInterval);
}
