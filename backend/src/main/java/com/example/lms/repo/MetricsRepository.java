package com.example.lms.repo;

import com.example.lms.domain.EnrollmentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MetricsRepository extends JpaRepository<EnrollmentMapping, Long> {

  @Query(value = "SELECT e.email_id as userId, " +
    "COUNT(CASE WHEN upper(e.status) = 'COMPLETED' THEN 1 END) as completedCourses, " +
    "AVG(e.progress_percent) as averageProgress, " +
    "COUNT(*) as totalEnrollments " +
    "FROM lms_schema.lms_user_trng_enrollment_mapping e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE ( e.completed_ts >= :startDate) " +
    "AND (e.completed_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR c.technology=:technology) " +
    "GROUP BY e.email_id " +
    "ORDER BY completedCourses DESC, averageProgress DESC " +
    "LIMIT :topN", nativeQuery = true)
  List<Object[]> findTopTrainees(@Param("startDate") OffsetDateTime startDate,
                                  @Param("endDate") OffsetDateTime endDate,
                                  @Param("category") String category,
                                  @Param("level") String level,
                                  @Param("technology") String technology,
                                  @Param("topN") Integer topN);

  @Query(value = "SELECT c.trng_id as trainingId, " +
    "c.trng_topic as topic, " +
    "c.category, " +
    "c.level_code as level, " +
    "c.rating, " +
    "COUNT(*) as enrollmentCount, " +
    "0 as completedCount, " +
    "0 as viewCount " +
    "FROM lms_schema.lms_user_trng_enrollment_mapping e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE upper(e.status) = 'COMPLETED' " +
    "AND ( e.completed_ts >= :startDate) " +
    "AND (e.completed_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR  c.technology=:technology) " +
    "GROUP BY c.trng_id, c.trng_topic, c.category, c.level_code, c.rating " +
    "ORDER BY CAST(c.rating AS DECIMAL) DESC NULLS LAST " +
    "LIMIT :topN", nativeQuery = true)
  List<Object[]> findTopRatedCourses(@Param("startDate") OffsetDateTime startDate,
                                      @Param("endDate") OffsetDateTime endDate,
                                      @Param("category") String category,
                                      @Param("level") String level,
                                      @Param("technology") String technology,
                                      @Param("topN") Integer topN);

  @Query(value = "SELECT c.trng_id as trainingId, " +
    "c.trng_topic as topic, " +
    "c.category, " +
    "c.level_code as level, " +
    "c.rating, " +
    "COUNT(*) as enrollmentCount, " +
    "0 as completedCount, " +
    "0 as viewCount " +
    "FROM lms_schema.lms_user_trng_enrollment_mapping e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE upper(e.status) != 'COMPLETED' " +
    "AND ( e.enrolled_ts >= :startDate) " +
    "AND (e.enrolled_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR  c.technology=:technology) " +
    "GROUP BY c.trng_id, c.trng_topic, c.category, c.level_code, c.rating " +
    "ORDER BY enrollmentCount DESC " +
    "LIMIT :topN", nativeQuery = true)
  List<Object[]> findTopEnrolledCourses(@Param("startDate") OffsetDateTime startDate,
                                         @Param("endDate") OffsetDateTime endDate,
                                         @Param("category") String category,
                                         @Param("level") String level,
                                         @Param("technology") String technology,
                                         @Param("topN") Integer topN);

  @Query(value = "SELECT c.trng_id as trainingId, " +
    "c.trng_topic as topic, " +
    "c.category, " +
    "c.level_code as level, " +
    "c.rating, " +
    "COUNT(*) as enrollmentCount, " +
    "0 as completedCount, " +
    "COUNT(e.view_ts) as viewCount " +
    "FROM lms_schema.lms_trng_search_hist e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE ( e.view_ts >= :startDate) " +
    "AND (e.view_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR c.technology=:technology) " +
    "GROUP BY c.trng_id, c.trng_topic, c.category, c.level_code, c.rating " +
    "ORDER BY viewCount DESC " +
    "LIMIT :topN", nativeQuery = true)
  List<Object[]> findTopViewedCourses(@Param("startDate") OffsetDateTime startDate,
                                       @Param("endDate") OffsetDateTime endDate,
                                       @Param("category") String category,
                                       @Param("level") String level,
                                       @Param("technology") String technology,
                                       @Param("topN") Integer topN);

  @Query(value = "SELECT c.trng_id as trainingId, " +
    "c.trng_topic as topic, " +
    "c.category, " +
    "c.level_code as level, " +
    "c.rating, " +
    "COUNT(*) as enrollmentCount, " +
    "COUNT(CASE WHEN upper(e.status) = 'COMPLETED' THEN 1 END) as completedCount, " +
    "0 as viewCount " +
    "FROM lms_schema.lms_user_trng_enrollment_mapping e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE upper(e.status) = 'COMPLETED' " +
    "AND ( e.completed_ts >= :startDate) " +
    "AND (e.completed_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR c.technology=:technology) " +
    "GROUP BY c.trng_id, c.trng_topic, c.category, c.level_code, c.rating " +
    "ORDER BY completedCount DESC " +
    "LIMIT :topN", nativeQuery = true)
  List<Object[]> findTopCompletedCourses(@Param("startDate") OffsetDateTime startDate,
                                          @Param("endDate") OffsetDateTime endDate,
                                          @Param("category") String category,
                                          @Param("level") String level,
                                          @Param("technology") String technology,
                                          @Param("topN") Integer topN);

  @Query(value = "SELECT e.email_id as userId, " +
    "c.trng_id as trainingId, " +
    "c.trng_topic as trainingTopic, " +
    "c.category, " +
    "c.level_code as level, " +
    "e.status, " +
    "e.progress_percent as progressPercent, " +
    "e.enrolled_ts as enrolledTs, " +
    "e.started_ts as startedTs, " +
    "e.updated_ts as lastAccessedTs " +
    "FROM lms_schema.lms_user_trng_enrollment_mapping e " +
    "JOIN lms_schema.lms_trng_summary c ON e.trng_id = c.trng_id " +
    "WHERE upper(e.status) = 'COMPLETED' " +
    "AND ( e.completed_ts >= :startDate) " +
    "AND (e.completed_ts <= :endDate) " +
    "AND (:category IS NULL OR c.category = :category) " +
    "AND (:level IS NULL OR c.level_code = :level) " +
    "AND (:technology IS NULL OR  c.technology=:technology) " +
    "ORDER BY e.enrolled_ts DESC", nativeQuery = true)
  List<Object[]> findUserTrainingDump(@Param("startDate") OffsetDateTime startDate,
                                       @Param("endDate") OffsetDateTime endDate,
                                       @Param("category") String category,
                                       @Param("level") String level,
                                       @Param("technology") String technology);
}
