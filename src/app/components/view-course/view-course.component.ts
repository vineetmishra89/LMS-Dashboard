import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Button, ButtonModule } from "primeng/button";
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { CourseService } from '../../services/course.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SkeletonModule } from 'primeng/skeleton';
import { EnrollmentService } from '../../services/enrollment.service';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { DataSharingService } from '../../services/data-sharing.service';
import { CourseMaster } from '../../models/course';
import { EnrollmentMapping } from '../../models/enrollments';
import { error } from 'console';
import { Globals } from '../shared/globals';

@Component({
  selector: 'app-view-course',
  standalone: true,
  imports: [ButtonModule, ProgressSpinnerModule, ConfirmDialogModule, DialogModule, ToastModule, SkeletonModule, OverlayPanelModule, InputGroupModule, InputGroupAddonModule],
  templateUrl: './view-course.component.html',
  styleUrl: './view-course.component.scss',
  providers: [MessageService, ConfirmationService]
})
export class ViewCourseComponent implements OnInit {

  userId: string = '';
  router = inject(Router);
  liked: boolean = false;
  courseService = inject(CourseService);
  enrollmentService = inject(EnrollmentService);
  messageService = inject(MessageService);
  confirmationService = inject(ConfirmationService);
  dataSharingService = inject(DataSharingService);
  route = inject(ActivatedRoute);
  globals = inject(Globals);
  courseDetail: any = null;
  totalLearners: number = 0;
  ratings: number = 0;
  trainingId: string = '0';
  loading: boolean = false;
  visible: boolean = false;
  message: string = '';
  progress: number = 0;
  enrollment!: EnrollmentMapping;

  hasEnrolled: boolean = false;
  courseRatings: any[] = [{ user: 'Ankit Bansal', ratings: 3, when: '3 weeks ago', comments: 'This course is good for intermediate level. Instructor explained topics very well' },
  { user: 'Vineet Mishra', ratings: 3, when: '1 day ago', comments: 'The instructor is taking a right approach teaching concepts very much to the point, crisp with practical applications and time limit of each lesson.' },
  { user: 'Satya Prakash Mishra', ratings: 3, when: 'Today', comments: 'Great Learning so far. Very Clear and detailed information with all resources available.' },
  { user: 'Rajib Bhattacharya', ratings: 3, when: '3 months ago', comments: 'Learnt the basics! Thank you so much.' }
  ]

  ngOnInit(): void {
    this.userId = this.globals.getUser().emailId;
    this.getCourseDetailsById();
  }

  enroll(event: Event) {
    // this.hasEnrolled = true;
    this.loading = true;

    const data = {
      userId: this.userId,
      courseId: Number(this.trainingId),
      enrollmentType: 'VOLUNTARY'
    }
    this.enrollmentService.enroll(data).subscribe({
      next: (res) => {
        this.message = 'Enrollment Successful.'
        this.visible = true;
        this.hasEnrolled = true;
        this.enrollment = res;
        console.log(res);
        this.loading = false;
      }
    });
  }

  unenroll(event: Event) {
    this.loading = true;

    this.enrollmentService.unenroll(this.enrollment.trainingEnrollmentId).subscribe({
      next: (res) => {
        this.message = 'Unenrollment Successful.'
        this.hasEnrolled = false;
        this.visible = true;
        this.enrollment = res;
        console.log(res);
        this.loading = false;
      }
    });
  }

  resume() {
    this.dataSharingService.sendData(this.courseDetail);
    this.router.navigate(['runningCourse', this.trainingId, this.enrollment.trainingEnrollmentId]);
  }

  getCourseDetailsById() {
    //this.loading = true;
    this.trainingId = this.route.snapshot.paramMap.get('trainingId') || '0';
    this.courseService.getCourseDetailsById(this.userId, this.trainingId).subscribe({
      next: (res) => {
        if (res) {
          this.courseDetail = res;
          this.enrollment = res.enrollmentMappings[0];
          this.totalLearners = res.enrollmentMappings.filter((x: any) => x.status.toUpperCase() === 'COMPLETED').length;
          this.ratings = Math.floor(Number(res.rating));
          this.hasEnrolled = res.enrollmentMappings.find((x: any) => x.userId === this.userId) || false;
          this.progress = res.lmsTrainingDetails.every((x: any) => x.moduleProgressPercentage === null);
        }
      }, error: (err: Error) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err['message'] });
      }
    })
  }
  numberToStars(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i);
  }

  convertStringToInt(str: string) {
    var Num = parseInt(str);
    return Num;
  }


}
