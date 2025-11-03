package com.example.lms.controller;


import com.example.lms.domain.FeedbackDetails;
import com.example.lms.repo.FeedbackRepository;
import com.example.lms.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin
public class FeedbackController {

     @Autowired
     private FeedbackRepository feedbackRepository;
     @Autowired
     private FeedbackService feedbackService;

     @GetMapping("/getFeedbackDetails/{enrollmentId}")
     public Optional<FeedbackDetails> getFeedbackDetails(@RequestParam Long enrollmentId){
         return feedbackService.geFeedbackDetails(enrollmentId);
     }

      @PostMapping("/saveFeedbackDetails")
     public boolean saveFeedbackDetails(@RequestBody FeedbackDetails feedbackDetails,@RequestParam String userId){
         return feedbackService.saveFeedbackDetails(feedbackDetails,userId);
     }

}
