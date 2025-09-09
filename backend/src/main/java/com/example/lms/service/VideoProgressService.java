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

    public VideoProgress updateProgress(String userId, String courseId, String lessonId,
                                      BigDecimal currentTime, BigDecimal duration, BigDecimal watchTime) {
        Optional<VideoProgress> existing = repository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);

        VideoProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
            progress.setWatchTime(progress.getWatchTime().add(watchTime));
        } else {
            progress = new VideoProgress();
            progress.setUserId(userId);
            progress.setCourseId(courseId);
            progress.setLessonId(lessonId);
            progress.setWatchTime(watchTime);
            progress.setCreatedAt(OffsetDateTime.now());
        }

        progress.setCurrentTime(currentTime);
        progress.setDuration(duration);
        progress.setCompleted(currentTime.compareTo(duration.multiply(BigDecimal.valueOf(0.9))) >= 0);
        progress.setLastWatchedAt(OffsetDateTime.now());
        progress.setUpdatedAt(OffsetDateTime.now());

        return repository.save(progress);
    }

    public Optional<VideoProgress> getProgress(String userId, String courseId, String lessonId) {
        return repository.findByUserIdAndCourseIdAndLessonId(userId, courseId, lessonId);
    }

    public List<VideoProgress> getCourseProgress(String userId, String courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId);
    }

    public Double getTotalLearningHours(String userId) {
        Double totalMinutes = repository.getTotalWatchTimeByUserId(userId);
        return totalMinutes != null ? totalMinutes : 0.0;
    }
}
