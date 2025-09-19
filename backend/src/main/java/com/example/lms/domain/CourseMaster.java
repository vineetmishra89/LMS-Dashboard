package com.example.lms.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_master")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NamedEntityGraph(
  name = "CourseMaster.withDetails",
  attributeNodes = @NamedAttributeNode("details"))
public class CourseMaster {

  @Id
  @Column(name = "id")
  private String trainingId;

  @Column(name = "training_name")
  private String trainingName;

  @Column(name = "category")
  private String category;

  //@ElementCollection
  //@CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
  @Column(name = "training_topic")
  private String topics;

  @Column(name = "description")
  private String description;

  @Column(name = "content")
  private String content;

  @Column(name = "instructor_name")
  private String instructorName;

  @Column(name = "duration")
  private Integer duration;

  @Column(name = "review_comments")
  private String reviewComments;

  @Column(name = "prerequisite")
  private String prerequisite;

  @Column(name = "level")
  private String level;

  @Column(name = "tools_needed")
  private String toolsNeeded;

  @OneToMany(
    mappedBy = "course",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  @JsonManagedReference("course-details")
  private List<CourseDetail> details = new ArrayList<>();

  // helpers to keep both sides in sync
  public void addDetail(CourseDetail d) {
    details.add(d);
    d.setCourse(this);
  }
  public void removeDetail(CourseDetail d) {
    details.remove(d);
    d.setCourse(null);
  }
}
