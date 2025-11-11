import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseDetail, CourseMaster } from '../../models/course';
import { CourseService } from '../../services/course.service';
import { UserService } from '../../services/user.service';
import { DataSharingService } from '../../services/data-sharing.service';
import { CommonModule } from '@angular/common';
import { EnrollmentService } from '../../services/enrollment.service';
import { EnrollmentMapping } from '../../models/enrollments';

@Component({
  selector: 'app-video-player-page',
  templateUrl: './video-player-page.component.html',
  styleUrls: ['./video-player-page.component.scss']
})
export class VideoPlayerPageComponent implements OnInit {
   course: CourseMaster | null = null;
   isLoading = true;
   dataSharingService = inject(DataSharingService);
   playCourseData: CourseMaster | null = null;
   selectedModule: CourseDetail | null = null;
   enrollmentMapping: EnrollmentMapping | null = null;
   
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService
  ) {}
  
  ngOnInit(): void {
    const courseId = this.route.snapshot.params['trainingId'];
    const trngEnrollmentId = this.route.snapshot.params['trngEnrollmentId'];
    const userId = 'test_trainee1@irissoftware.com';
    
      this.courseService.getCourseById(courseId,userId).subscribe({
        next: (course) => {
          this.isLoading = false;
          this.playCourseData = course;
          this.selectedModule = course.lmsTrainingDetails[0];
        },
        error: (error) => {
          console.error('Failed to load course:', error);
          this.router.navigate(['/dashboard']);
        }
      });

      this.enrollmentService.getEnrollmentById(trngEnrollmentId).subscribe({
        next: (enrollmentMapping) => {
          this.enrollmentMapping = enrollmentMapping;
        }
      })
  }

  moduleSelected(selectedModule: any, index: number) {
    this.selectedModule = selectedModule

  }
  
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
