package com.example.lms.service;

import com.example.lms.domain.VideoProgress;
import com.example.lms.repo.VideoProgressRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoProgressService {
    private final VideoProgressRepository repository;

    public VideoProgressService(VideoProgressRepository repository) {
        this.repository = repository;
    }

    public VideoProgress updateProgress(VideoProgress progress) {
        Optional<VideoProgress> existing = repository.findByUserIdAndCourseIdAndLessonId(progress.getUserId(), progress.getCourseId(), progress.getLessonId());

      VideoProgress existingProgress = null;
        if (existing.isPresent() && !existing.get().getCompleted()) {
            existingProgress = existing.get();
            existingProgress.setWatchTime(existingProgress.getWatchTime().add(progress.getWatchTime()));
        } else {
          existingProgress = new VideoProgress();
          existingProgress.setUserId(progress.getUserId());
          existingProgress.setCourseId(progress.getCourseId());
          existingProgress.setLessonId(progress.getLessonId());
          existingProgress.setWatchTime(progress.getWatchTime());
          existingProgress.setCreatedAt(OffsetDateTime.now());
        }

      existingProgress.setCurrentTime(progress.getCurrentTime());
      existingProgress.setDuration(progress.getDuration());
        //progress.setCompleted(progress.getCurrentTime().compareTo(progress.getDuration().multiply(BigDecimal.valueOf(0.9))) >= 0);
      existingProgress.setCompleted(progress.getCompleted());
      existingProgress.setLastWatchedAt(OffsetDateTime.now());
      existingProgress.setUpdatedAt(OffsetDateTime.now());

        return repository.save(existingProgress);
    }

    public Optional<VideoProgress> getProgress(String userId, Integer courseId, Integer lessonId) {
        return repository.findByUserIdAndCourseIdAndLessonIdAndCompleted(userId, courseId, lessonId,false);
    }

    public Double getTotalLearningHours(String userId) {
        Double totalMinutes = repository.getTotalWatchTimeByUserId(userId);
        return totalMinutes != null ? totalMinutes : 0.0;
    }
}
