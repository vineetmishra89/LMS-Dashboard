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
    public VideoProgress updateProgress(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String courseId = String.valueOf((Integer) request.get("courseId"));
        String lessonId = String.valueOf((Integer) request.get("lessonId"));
        BigDecimal currentTime = new BigDecimal(request.get("currentTime").toString());
        BigDecimal duration = new BigDecimal(request.get("duration").toString());
        BigDecimal watchTime = new BigDecimal(request.get("watchTime").toString());

        return service.updateProgress(userId, courseId, lessonId, currentTime, duration, watchTime);
    }

    @GetMapping("/{userId}/{courseId}/{lessonId}")
    public Optional<VideoProgress> getProgress(@PathVariable String userId,
                                              @PathVariable String courseId,
                                              @PathVariable String lessonId) {
        return service.getProgress(userId, courseId, lessonId);
    }

    @GetMapping("/learning-hours/{userId}")
    public Map<String, Double> getLearningHours(@PathVariable String userId) {
        return Map.of("totalHours", service.getTotalLearningHours(userId));
    }
}
