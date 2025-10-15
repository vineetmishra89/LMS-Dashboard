package com.example.lms.service;

import com.example.lms.domain.CourseDetail;
import com.example.lms.domain.CourseSummary;
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
    public List<CourseDetail> search(String category, String topic, String instructor, String level ) {
      CourseSummary course = null;
      List<CourseDetail> courseDetailList = null;

        try{
            course = courseRepository.getCourseDetail( category, topic, instructor);
            if(course!=null)
           return course.getLmsTrainingDetails();
        }catch(Exception ex){
            log.error("Exception occurred : ",ex);
        }
        return new ArrayList<>();

    }
    }

