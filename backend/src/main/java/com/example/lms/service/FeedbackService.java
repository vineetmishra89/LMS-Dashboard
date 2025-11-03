package com.example.lms.service;

import com.example.lms.domain.FeedbackDetails;
import com.example.lms.repo.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public boolean saveFeedbackDetails(FeedbackDetails feedbackDetails,String userId) {
          feedbackDetails.setCreatedBy(userId);
          feedbackDetails.setUpdatedBy(userId);
          feedbackDetails.setCreatedTs(OffsetDateTime.now());
          feedbackDetails.setUpdatedTs(OffsetDateTime.now());
          feedbackRepository.save(feedbackDetails);
          return true;
    }
    public Optional<FeedbackDetails> geFeedbackDetails(Long enrollmentId) {
        return feedbackRepository.findEnrollmentId(enrollmentId);
    }

}
