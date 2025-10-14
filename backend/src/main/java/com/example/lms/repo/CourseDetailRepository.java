package com.example.lms.repo;

import com.example.lms.domain.CourseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseDetailRepository extends JpaRepository<CourseDetail, String>, JpaSpecificationExecutor<CourseDetail> {
  // Use the entity graph so details are fetched with masters
  @Override
  List<CourseDetail> findAll();

  @Override
  Optional<CourseDetail> findById(String id);
}
