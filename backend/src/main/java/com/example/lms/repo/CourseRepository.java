package com.example.lms.repo;

import com.example.lms.domain.CourseSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseSummary, Long>, JpaSpecificationExecutor<CourseSummary> {
  // Use the entity graph so details are fetched with masters
  @Override
  @EntityGraph(value = "CourseSummary.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  List<CourseSummary> findAll();

  @Override
  @EntityGraph(value = "CourseSummary.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Optional<CourseSummary> findById(Long id);

  // Alternative fetch-join (handy for ad-hoc calls)
  @Query("select distinct cm from CourseSummary cm left join fetch cm.details")
  List<CourseSummary> findAllWithDetailsFetchJoin();




}
