package com.example.lms.repo;

import com.example.lms.domain.CourseSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseSummary, Long>, JpaSpecificationExecutor<CourseSummary> {
  // Use the entity graph so details are fetched with masters
  @Override
  @EntityGraph(value = "CourseSummary.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  List<CourseSummary> findAll();

  @Query("select cs from CourseSummary cs where cs.trainingId = :trainingId")
  CourseSummary findCourseSummaryByTrainingId(@Param("trainingId") Long trainingId);

  @EntityGraph(attributePaths = {
    "lmsTrainingDetails",
    "enrollmentMappings",
    "enrollmentMappings.enrollmentDetailsList"
  })
  @Query("select distinct cs from CourseSummary cs left join fetch cs.lmsTrainingDetails cd left join fetch cs.enrollmentMappings em left join fetch em.enrollmentDetailsList emd where cs.trainingId = :trainingId and em.userId=:userId")
  CourseSummary findEnrolledCourseByIdAndUserId(@Param("trainingId") Long trainingId, @Param("userId") String userId);

  // Alternative fetch-join (handy for ad-hoc calls)
  @Query("select distinct cm from CourseSummary cm left join fetch cm.details")
  List<CourseSummary> findAllWithDetailsFetchJoin();

  @Query(value = "select distinct category,trng_topic,trng_id from lms_schema.lms_trng_summary order by category,trng_topic", nativeQuery = true)
  List<Object[]> getAllTrainings();

  @Query(value = "select distinct category from lms_schema.lms_trng_summary order by category", nativeQuery = true)
  List<Object> getAllCategories();

  @Query(value = "select distinct level_code from lms_schema.lms_trng_summary order by level_code", nativeQuery = true)
  List<Object> getAllLevels();

  /**
   * Find CourseSummary by folder_path (case-insensitive).
   * Used for matching folders by name.
   */
  Optional<CourseSummary> findByFolderPathIgnoreCase(String folderPath);

  /**
   * Find CourseSummary by exact folder_path.
   * Used for matching folders by URL.
   */
  Optional<CourseSummary> findByFolderPath(String folderPath);

}
