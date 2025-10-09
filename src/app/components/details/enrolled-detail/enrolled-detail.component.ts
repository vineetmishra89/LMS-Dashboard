import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { CourseDetail, CourseMaster } from '../../../models/course';
import { Enrollment } from '../../../models/enrollment';
import { CourseService } from '../../../services/course.service';
import { EnrollmentService } from '../../../services/enrollment.service';

@Component({
  selector: 'app-enrolled-detail',
  templateUrl: './enrolled-detail.component.html',
  styleUrls: ['./enrolled-detail.component.scss']
})
export class EnrolledDetailComponent implements OnInit {
  enrolledCourses$!: Observable<CourseMaster[]>;
  enrollments$!: Observable<Enrollment[]>;
  userId: string = 'vm02102';

  constructor(
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.enrolledCourses$ = this.courseService.getEnrolledCourses(this.userId);
    this.enrollments$ = this.enrollmentService.getUserEnrollments(this.userId);
  }

  onVideoClick(course: CourseDetail): void {
    this.router.navigate(['/video-player', course.trainingDetailId]);
  }

  getEnrollmentFor(courseId: string): Observable<Enrollment | undefined> {
    return this.enrollments$.pipe(
      map(enrollments => enrollments.find(e => e.courseId === courseId))
    );
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = Math.floor(minutes % 60);
    
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  }
}
