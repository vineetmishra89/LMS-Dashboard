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
      VideoProgress progress = prepareVideoProgress(request);
        return service.updateProgress(progress);
    }

    private VideoProgress prepareVideoProgress(Map<String, Object> request){
      VideoProgress progress = new VideoProgress();
      progress.setUserId((String) request.get("userId"));
      progress.setCourseId((Integer)request.get("courseId"));
      progress.setLessonId((Integer) request.get("lessonId"));
      progress.setCurrentTime(new BigDecimal(request.get("currentTime").toString()));
      progress.setDuration(new BigDecimal(request.get("duration").toString()));
      progress.setWatchTime(new BigDecimal(request.get("watchTime").toString()));
      progress.setCompleted((Boolean) request.get("completed"));
      return progress;
    }

    @GetMapping("/{userId}/{courseId}/{lessonId}")
    public Optional<VideoProgress> getProgress(@PathVariable String userId,
                                              @PathVariable Integer courseId,
                                              @PathVariable Integer lessonId) {
        return service.getProgress(userId, courseId, lessonId);
    }

    @GetMapping("/learning-hours/{userId}")
    public Map<String, Double> getLearningHours(@PathVariable String userId) {
        return Map.of("totalHours", service.getTotalLearningHours(userId));
    }
}
