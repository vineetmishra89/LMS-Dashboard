package com.example.lms.repo;

import com.example.lms.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
  List<Course> findByCategoryIgnoreCase(String category);
  List<Course> findByInstructorNameIgnoreCase(String instructorName);
}
