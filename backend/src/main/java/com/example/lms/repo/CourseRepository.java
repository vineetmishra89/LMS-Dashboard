package com.example.lms.repo;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseMaster;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseMaster, String>, JpaSpecificationExecutor<CourseMaster> {
  // Use the entity graph so details are fetched with masters
  @Override
  @EntityGraph(value = "CourseMaster.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  List<CourseMaster> findAll();

  @Override
  @EntityGraph(value = "CourseMaster.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Optional<CourseMaster> findById(String id);

  // Alternative fetch-join (handy for ad-hoc calls)
  @Query("select distinct cm from CourseMaster cm left join fetch cm.details")
  List<CourseMaster> findAllWithDetailsFetchJoin();
}
