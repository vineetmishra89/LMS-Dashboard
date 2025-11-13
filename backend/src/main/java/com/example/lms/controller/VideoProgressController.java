package com.example.lms.controller;

import com.example.lms.domain.VideoProgress;
import com.example.lms.service.VideoProgressService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/video-progress")
@CrossOrigin
public class VideoProgressController {
    private final VideoProgressService service;

    public VideoProgressController(VideoProgressService service) {
        this.service = service;
    }

    @PostMapping("/update")
    public VideoProgress updateProgress(@RequestBody VideoProgress progress) {
        return service.updateProgress(progress);
    }

    @GetMapping("/{enrollmentDetailsId}")
    public Optional<VideoProgress> getProgress(@PathVariable Integer enrollmentDetailsId) {
        return service.getProgress(enrollmentDetailsId);
    }

    @GetMapping("/learning-hours/{userId}")
    public Map<String, Double> getLearningHours(@PathVariable String userId) {
        return Map.of("totalHours", service.getTotalLearningHours(userId));
    }
}
