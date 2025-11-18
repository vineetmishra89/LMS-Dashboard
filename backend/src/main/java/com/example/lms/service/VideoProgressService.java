package com.example.lms.service;

import com.example.lms.constant.ReviewStatus;
import com.example.lms.domain.VideoProgress;
import com.example.lms.repo.EnrollmentDetailsRepository;
import com.example.lms.repo.EnrollmentRepository;
import com.example.lms.repo.VideoProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoProgressService {
    private final VideoProgressRepository repository;

    private EnrollmentDetailsRepository enrollmentDetailsRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public VideoProgressService(VideoProgressRepository repository, EnrollmentDetailsRepository enrollmentDetailsRepository) {
        this.repository = repository;
        this.enrollmentDetailsRepository = enrollmentDetailsRepository;
    }

    public VideoProgress updateProgress(VideoProgress progress) {
        Optional<VideoProgress> existing = repository.findByTrainingEnrollmentDtlIdAndCompleted(progress.getTrainingEnrollmentDtlId().intValue(),false);

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
          existingProgress.setTrainingEnrollmentDtlId(progress.getTrainingEnrollmentDtlId());
        }

      existingProgress.setCurrentTime(progress.getCurrentTime());
      existingProgress.setDuration(progress.getDuration());
        //progress.setCompleted(progress.getCurrentTime().compareTo(progress.getDuration().multiply(BigDecimal.valueOf(0.9))) >= 0);
      existingProgress.setCompleted(progress.getCompleted());
      existingProgress.setLastWatchedAt(OffsetDateTime.now());
      existingProgress.setUpdatedAt(OffsetDateTime.now());

      VideoProgress videoProgress = repository.save(existingProgress);

      if(progress.getCompleted()){
        this.enrollmentDetailsRepository.updateEnrollmentStatusByEnrollmentDtlId(progress.getTrainingEnrollmentDtlId());
        long count = this.enrollmentDetailsRepository.countEnrollmentByStatusAndEnrollmentId(progress.getCourseId(), progress.getUserId());
        if(count==0){
           this.enrollmentRepository.updateEnrollMappingByUseridAndTrngId(ReviewStatus.PENDING_LND_REVIEW.name(),progress.getCourseId(), progress.getUserId());
        }
      }

        return videoProgress;
    }

    public Optional<VideoProgress> getProgress(Integer enrollmentDetailsId) {
        return repository.findByTrainingEnrollmentDtlIdAndCompleted(enrollmentDetailsId,false);
    }

    public Double getTotalLearningHours(String userId) {
        Double totalMinutes = repository.getTotalWatchTimeByUserId(userId);
        return totalMinutes != null ? totalMinutes : 0.0;
    }
}
