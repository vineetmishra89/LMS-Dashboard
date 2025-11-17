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
  globals = inject(Globals);
  playCourseData: CourseMaster | null = null;
  selectedModule: CourseDetail | null = null;
  enrollmentMapping: EnrollmentMapping | null = null;
  selectedEnrollmentModule: EnrollmentDetails | null = null;
  videoPageReady: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService
  ) { }

  ngOnInit(): void {
    const courseId = this.route.snapshot.params['trainingId'];
    console.log("courseId : " + courseId);
    const trngEnrollmentId = this.route.snapshot.params['trngEnrollmentId'];
    console.log("trngEnrollmentId : " + trngEnrollmentId);
    const userId = this.globals.getUser().emailId;

    forkJoin({
      course: this.courseService.getCourseById(courseId, userId),
      enrollment: this.enrollmentService.getEnrollmentById(trngEnrollmentId)
    }).subscribe({
      next: ({ course, enrollment }) => {
        this.isLoading = false;
        this.course = course;
        this.playCourseData = course;
        //   this.selectedModule = course.lmsTrainingDetails[0];

        this.enrollmentMapping = enrollment;
        const runningCourse = this.getRunningModule(this.enrollmentMapping!.enrollmentDetailsList);
        this.selectedEnrollmentModule = this.enrollmentMapping!.enrollmentDetailsList[runningCourse];
        this.selectedModule = this.enrollmentMapping!.enrollmentDetailsList[runningCourse].courseDetail;
        console.log('CourseDetail : ' + this.enrollmentMapping!.enrollmentDetailsList[runningCourse].courseDetail);
        this.videoPageReady = true;
        console.log("Video player page loaded successfully");
      },
      error: (error) => {
        console.error('Failed to load video player page:', error);
        this.router.navigate(['/dashboard']);
      }
    });
  }

  getRunningModule(list: any): number {
    const sortedList = list.sort((a: any, b: any) => a.moduleId - b.moduleId);
    const statusMap = sortedList.map((item: any) => item.status.toUpperCase() === 'COMPLETED');
    const lastCompletedIndex_map = statusMap.lastIndexOf(true);
    if (lastCompletedIndex_map !== -1) {
      return lastCompletedIndex_map;
    } else {
      return 0;
    }


  }

  moduleSelected(selectedModule: any, index: number, status: string) {
    if (status.toUpperCase() !== 'COMPLETED') {
      return;
    }
    this.selectedModule = selectedModule;
    this.selectedEnrollmentModule = this.enrollmentMapping!.enrollmentDetailsList[index];
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  currentVideCompleted(event: any) {
    console.log(event);
    if (this.enrollmentMapping) {
      const currentIndex = this.enrollmentMapping.enrollmentDetailsList.findIndex((x: any) => x.moduleId === this.selectedModule?.moduleId);

      this.enrollmentMapping!.enrollmentDetailsList[currentIndex].status = 'Completed';
      console.log(this.enrollmentMapping!.enrollmentDetailsList);
      this.selectedModule = this.enrollmentMapping!.enrollmentDetailsList[currentIndex + 1].courseDetail;
    }


  }
}
