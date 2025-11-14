import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseDetail, CourseMaster } from '../../models/course';
import { CourseService } from '../../services/course.service';
import { UserService } from '../../services/user.service';
import { DataSharingService } from '../../services/data-sharing.service';
import { CommonModule } from '@angular/common';
import { EnrollmentService } from '../../services/enrollment.service';
import { EnrollmentDetails, EnrollmentMapping } from '../../models/enrollments';
import { forkJoin } from 'rxjs';
import { Globals } from '../../components/shared/globals';

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
   selectedEnrollmentModule: EnrollmentDetails | null = null;
   videoPageReady: boolean = false;
   globals = inject(Globals);
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService
  ) {}
  
  ngOnInit(): void {
    const courseId = this.route.snapshot.params['trainingId'];
    console.log("courseId : "+courseId);
    const trngEnrollmentId = this.route.snapshot.params['trngEnrollmentId'];
    console.log("trngEnrollmentId : "+trngEnrollmentId);
    const userId = this.globals.getUser().emailId;
    
    forkJoin({
      course: this.courseService.getCourseById(courseId,userId),
      enrollment: this.enrollmentService.getEnrollmentById(trngEnrollmentId)
    }).subscribe({
        next: ({course, enrollment}) => {
          this.isLoading = false;
          this.course = course;
          this.playCourseData = course;
          this.selectedModule = course.lmsTrainingDetails[0];

          this.enrollmentMapping = enrollment;
          this.selectedEnrollmentModule = this.enrollmentMapping!.enrollmentDetailsList[0];
          console.log('CourseDetail : '+this.enrollmentMapping!.enrollmentDetailsList[0].courseDetail);
          this.videoPageReady = true;
          console.log("Video player page loaded successfully");
        },
        error: (error) => {
          console.error('Failed to load video player page:', error);
          this.router.navigate(['/dashboard']);
        }
      });
  }

  moduleSelected(selectedModule: any, index: number) {
    this.selectedModule = selectedModule;
    this.selectedEnrollmentModule = this.enrollmentMapping!.enrollmentDetailsList[index];
  }
  
  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
