package com.example.lms.service;

import com.example.lms.domain.CourseSummary;
import com.example.lms.dto.SearchDto;
import com.example.lms.dto.SearchFilterDto;
import com.example.lms.dto.TrainingNameDto;
import com.example.lms.repo.TrainerRepository;
import com.example.lms.repo.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class SearchService {
  private final CourseRepository courseRepository;
  private final TrainerRepository trainerRepository;

  @Autowired
  private EntityManager entityManager;
  @Autowired
  private Session session;

  private static final Logger log = LoggerFactory.getLogger(SearchService.class);

  public SearchService(CourseRepository courseRepository, TrainerRepository trainerRepository) {
    this.courseRepository = courseRepository;
    this.trainerRepository = trainerRepository;
  }

  public List<CourseSummary> search(String category, String topics, String instructor, String level) {
    StringBuilder hql = new StringBuilder("select  cm from CourseSummary cm  join fetch cm.lmsTrainingDetails ltd join  ltd.trainerDetails td where 1=1");

    if (category != null) {
      hql.append(" AND cm.category = :category");
    }
    if (topics != null) {
      hql.append(" AND cm.topics = :topics");
    }
    if (instructor != null) {
      hql.append(" AND td.emailid =:instructor");
    }
    if (level != null) {
      hql.append(" AND cm.level =:level");
    }

    Query query = session.createQuery(hql.toString(), CourseSummary.class);
    if (category != null) {
      query.setParameter("category", category);
    }
    if (topics != null) {
      query.setParameter("topics", topics);
    }
    if (instructor != null) {
      query.setParameter("instructor", instructor);
    }
    if (level != null) {
      query.setParameter("level", level);
    }
    List<CourseSummary> results = query.getResultList();
    return results;

  }

  /*
  Get all drop downs required for search drop downs
   */
  public SearchFilterDto getSearchList() {
    SearchFilterDto response = new SearchFilterDto();

    response.setTrainingNameList(getTrainingList());
    //log.info("Got training list -- " + response.getTrainingNameList.size());

    response.setCategoryList(getAllCategories());

    response.setLevelList(getAllLevels());

    response.setTrainerNameList(getAllTrainers());


    return response;

  }

  private List<TrainingNameDto> getTrainingList() {
    try {
      List<Object[]> results = courseRepository.getAllTrainings();
      if (null != results) {
        log.info("got results");
        return results.stream()
          .map(row -> new TrainingNameDto(
            (String)row[0],(String)row[1]
          ))
          .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    } catch (Exception e) {
      log.error("Error occured --? ");
      e.printStackTrace();
      return Collections.emptyList();
    }

  }

  private List<String> getAllCategories() {
    try {
      List<Object> results = courseRepository.getAllCategories();
      if (null != results) {
        log.info("got results");
        return results.stream()
          .map(obj -> (String) obj)
          .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    } catch (Exception e) {
      log.error("Error occured --? ");
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

    private List<String> getAllLevels() {
      try {
        List<Object> results = courseRepository.getAllLevels();
        if (null != results) {
          log.info("got results");
          return results.stream()
            .map(obj -> (String) obj)
            .collect(Collectors.toList());
        } else {
          return Collections.emptyList();
        }
      } catch (Exception e) {
        log.error("Error occured --? ");
        e.printStackTrace();
        return Collections.emptyList();
      }

  }
    private List<String> getAllTrainers() {
      try {
        List<Object> results = trainerRepository.getAllTrainers();
        if (null != results) {
          log.info("got results");
          return results.stream()
            .map(obj -> (String) obj)
            .collect(Collectors.toList());
        } else {
          return Collections.emptyList();
        }
      } catch (Exception e) {
        log.error("Error occured --? ");
        e.printStackTrace();
        return Collections.emptyList();
      }

    }
}

