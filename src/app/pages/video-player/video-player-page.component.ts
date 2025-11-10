import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseMaster } from '../../models/course';
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
  enrollmentMapping: EnrollmentMapping | null = null;
  isLoading = true;
  dataSharingService = inject(DataSharingService);
  playCourseData: CourseMaster | null = null;
  selectedModule: any = null;
   
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService
  ) {}
  
  ngOnInit(): void {
    const courseId = this.route.snapshot.params['trainingId'];
    const userId = 'test_trainee1@irissoftware.com';
    
    const navigationState = this.router.getCurrentNavigation()?.extras?.state || 
                           (history.state && history.state.courseData ? history.state : null);
    
    if (navigationState && navigationState.enrollmentMapping) {
      console.log('Using Enrollment data from navigation state:', navigationState.enrollmentMapping);
      this.enrollmentMapping = navigationState.enrollmentMapping;
      this.isLoading = false;
    } else {
      this.courseService.getCourseById(courseId,userId).subscribe({
        next: (enrollmentMapping) => {
          this.enrollmentMapping = enrollmentMapping;
          this.isLoading = false;
          this.selectedModule = enrollmentMapping.enrollmentDetailsList[0].courseDetail;
        },
        error: (error) => {
          console.error('Failed to load course:', error);
          this.router.navigate(['/dashboard']);
        }
      });
    }

    this.dataSharingService.getData().subscribe(data => {
      this.playCourseData = data;
    });
  }

  moduleSelected(selectedModule: any, index: number) {
    this.selectedModule = selectedModule

  }
  
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
