package com.example.lms.service;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.SearchDto;
import com.example.lms.repo.CourseDetailRepository;
import com.example.lms.repo.CourseRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    private final CourseRepository courseRepository;

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    public SearchService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    public SearchDto search(String category, String topic, String instructor, String level ) {
      CourseSummary course = null;
      SearchDto searchDto = new SearchDto();

        try{
            course = courseRepository.getCourseDetail( category, topic, instructor,level);
            if(course!=null){
              searchDto.setTrainingName(course.getTopics());
              searchDto.setCategory(course.getCategory());
              searchDto.setDuration(course.getDuration());
              searchDto.setTrainingDesc(course.getDetails());
              searchDto.setLevel(course.getLevel());
              searchDto.setTrainerName(instructor);
              searchDto.setRating(course.getRating());
              searchDto.setCourseDetailList(course.getLmsTrainingDetails());
            }

        }catch(Exception ex){
            log.error("Exception occurred : ",ex);
        }
        return searchDto;

    }
    }

