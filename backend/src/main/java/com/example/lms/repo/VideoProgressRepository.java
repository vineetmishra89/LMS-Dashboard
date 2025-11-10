package com.example.lms.repo;

import com.example.lms.domain.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
    Optional<VideoProgress> findByUserIdAndCourseIdAndLessonId(String userId, String courseId, String lessonId);
  Optional<VideoProgress> findByUserIdAndCourseIdAndLessonIdAndCompleted(String userId, String courseId, String lessonId, Boolean completed);

    @Query("SELECT SUM(vp.watchTime) FROM VideoProgress vp WHERE vp.userId = ?1")
    Double getTotalWatchTimeByUserId(String userId);
}
