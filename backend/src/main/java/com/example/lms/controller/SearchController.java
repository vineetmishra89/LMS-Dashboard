package com.example.lms.controller;

import com.example.lms.domain.CourseDetail;
import com.example.lms.service.CourseService;
import com.example.lms.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/searchCourse")
@CrossOrigin
public class SearchController {

    private final SearchService searchService;
    public SearchController(SearchService searchService) { this.searchService = searchService; }

    @GetMapping("/getCourseDetail")
    public List<CourseDetail> getCourseDetail(String category, String topic, String instructor, String level) {
        return searchService.search(category, topic, instructor, level);
    }
}
