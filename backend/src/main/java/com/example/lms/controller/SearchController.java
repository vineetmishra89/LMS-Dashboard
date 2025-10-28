package com.example.lms.controller;


import com.example.lms.dto.SearchDto;
import com.example.lms.dto.SearchFilterDto;
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
    public List<SearchDto> getCourseDetail( @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String level,
                                      @RequestParam(required = false) String instructor,
                                      @RequestParam(required = false) String topic){
        return searchService.search(category, topic, instructor, level);
    }

  @GetMapping("/getSearchList")
  public SearchFilterDto getSearchList(){
    return searchService.getSearchList();
  }
}
