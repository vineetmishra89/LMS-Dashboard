package com.example.lms.service;

import com.example.lms.repo.TrainingDetailsRepository;
import com.example.trainingdetails.utill.ExcelReader;
import com.example.lms.dto.TrainingDetails;
import com.example.lms.repo.TrainingDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainingDetailsService {

    private final TrainingDetailsRepository repository;

    public void saveExcelDatawithHeaders(MultipartFile file) throws Exception {
        List<Map<String, String>> rows = ExcelReader.readExcel(file.getInputStream());

        // Simple header validation (only done once in ExcelReader)
        for (Map<String, String> row : rows) {
            TrainingDetails trainingDetails = new TrainingDetails();
            trainingDetails.setTraining_Name(row.getOrDefault("Training Name", ""));
            trainingDetails.setDescription(row.getOrDefault("Description", ""));
            trainingDetails.setContent(row.getOrDefault("Outline/Content", ""));
            trainingDetails.setDuration(row.getOrDefault("Duration", ""));
            trainingDetails.setTrainers_current_feedback(row.getOrDefault("Trainers Current feedback", ""));
            trainingDetails.setCurrent_user_feedback(row.getOrDefault("Current User feedback", ""));
            trainingDetails.setReview_comments(row.getOrDefault("Review Comments", ""));
            trainingDetails.setTrainer_Name(row.getOrDefault("Trainer/Name", ""));
            trainingDetails.setPrerequisite(row.getOrDefault("Pre-requisite", ""));
            trainingDetails.setLevel(row.getOrDefault("Level(Beginner or Advance)", ""));
            trainingDetails.setTools_needed(row.getOrDefault("Tools needed", ""));
            trainingDetails.setRecording_link(row.getOrDefault("Recording Link", ""));
            repository.save(trainingDetails);
        }
    }

    public List<TrainingDetails> getAllTrainingData() {
        return repository.findAll();
    }
}
